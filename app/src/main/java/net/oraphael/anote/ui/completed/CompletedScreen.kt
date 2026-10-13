package net.oraphael.anote.ui.completed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.CompletedDay
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.formatTime
import net.oraphael.anote.ui.list.AreaChip
import net.oraphael.anote.ui.rememberToday
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dayTitle = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", AppLocale)

// Tarefas concluídas, por dia. Daqui dá para desfazer uma conclusão feita por engano.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedScreen(app: AnoteApp, onClose: () -> Unit) {
    val today by rememberToday()
    val days by remember { app.completedSource.observe() }.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val undo: (ListEntry) -> Unit = { item ->
        scope.launch {
            app.entryRepository.uncomplete(item.entry.id)
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(resources.getString(R.string.completed_reopened, item.entry.text))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.completed_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        val current = days ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.l),
        ) {
            item(key = "intro", contentType = "intro") {
                Text(
                    text = stringResource(
                        if (current.isEmpty()) R.string.completed_empty else R.string.completed_intro
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalColorTokens.current.onSurfaceVariant,
                )
            }
            current.forEach { day -> day(day, today, undo) }
        }
    }
}

private fun LazyListScope.day(
    day: CompletedDay,
    today: LocalDate,
    onUndo: (ListEntry) -> Unit,
) {
    item(key = "day-${day.date}", contentType = "header") {
        Text(
            text = when (day.date) {
                today -> stringResource(R.string.completed_today)
                today.minusDays(1) -> stringResource(R.string.completed_yesterday)
                else -> dayTitle.format(day.date).replaceFirstChar { it.titlecase(AppLocale) }
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .animateItem()
                .padding(top = Spacing.l, bottom = Spacing.s),
        )
    }
    items(day.entries, key = { it.entry.id }, contentType = { "entry" }) { item ->
        CompletedRow(item, onUndo, Modifier.animateItem())
    }
}

@Composable
private fun CompletedRow(item: ListEntry, onUndo: (ListEntry) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    val completedAt = requireNotNull(item.entry.completedAt)
    val time = formatTime(LocalTime.ofInstant(completedAt, ZoneId.systemDefault()))
    val undoDescription = stringResource(R.string.completed_undo_description, item.entry.text)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = Spacing.m)) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(vertical = Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(item.entry.text, style = MaterialTheme.typography.bodyLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    item.areas.forEach { AreaChip(it) }
                    Text(
                        text = stringResource(R.string.completed_at, time),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            TextButton(
                onClick = { onUndo(item) },
                modifier = Modifier
                    .padding(horizontal = Spacing.s)
                    .semantics { contentDescription = undoDescription },
            ) { Text(stringResource(R.string.complete_undo)) }
        }
    }
}
