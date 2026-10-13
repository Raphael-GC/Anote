package net.oraphael.anote.ui.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.SortOrder
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.rememberToday
import net.oraphael.anote.ui.entry.CaptureButton
import net.oraphael.anote.ui.entry.EditEntrySheet
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(
    app: AnoteApp,
    onOpenSettings: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenWeek: () -> Unit,
    onOpenDashboard: () -> Unit,
    onCapture: () -> Unit,
) {
    val today by rememberToday()
    var order by rememberSaveable { mutableStateOf(SortOrder.OLDEST) }
    val list by remember(today, order) { app.mainListSource.observe(today, order) }.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    // Concluir sem querer acontece: a mensagem oferece desfazer.
    val complete: (Long) -> Unit = { id ->
        val text = list?.let { (it.focuses + it.tasks).find { e -> e.entry.id == id }?.entry?.text }.orEmpty()
        scope.launch {
            app.entryRepository.complete(id)
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = resources.getString(R.string.complete_done, text),
                actionLabel = resources.getString(R.string.complete_undo),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) app.entryRepository.uncomplete(id)
        }
    }
    var editing by remember { mutableStateOf<ListEntry?>(null) }
    editing?.let { EditEntrySheet(app, it, onDismiss = { editing = null }) }
    val open: (ListEntry) -> Unit = { editing = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    TextButton(onClick = onOpenReview) { Text(stringResource(R.string.review_daily_open)) }
                    var menu by remember { mutableStateOf(false) }
                    IconButton(onClick = { menu = true }) {
                        Icon(AppIcons.More, contentDescription = stringResource(R.string.more_options))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.review_sunday_title)) },
                            onClick = {
                                menu = false
                                onOpenWeek()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.dashboard_title)) },
                            onClick = {
                                menu = false
                                onOpenDashboard()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings)) },
                            onClick = {
                                menu = false
                                onOpenSettings()
                            },
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = { CaptureButton(onCapture) },
    ) { innerPadding ->
        val current = list ?: return@Scaffold
        if (current.isEmpty) {
            EmptyState(Modifier.padding(innerPadding).padding(top = Spacing.xxl))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            // Espaço no fim para o botão "Anotar" não cobrir o último item.
            contentPadding = PaddingValues(start = Spacing.l, top = Spacing.l, end = Spacing.l, bottom = 96.dp),
        ) {
            item(key = "sort", contentType = "sort") {
                SortSelector(order, onChange = { order = it })
            }
            section(R.string.list_focuses_title, current.focuses, today, complete, open)
            section(R.string.list_tasks_title, current.tasks, today, complete, open)
            section(R.string.list_notes_title, current.notes, today, complete, open)
        }
    }
}

private fun LazyListScope.section(
    title: Int,
    entries: List<ListEntry>,
    today: LocalDate,
    onComplete: (Long) -> Unit,
    onOpen: (ListEntry) -> Unit,
) {
    if (entries.isEmpty()) return
    item(key = "header-$title", contentType = "header") {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .animateItem()
                .padding(top = Spacing.l, bottom = Spacing.s),
        )
    }
    items(entries, key = { it.entry.id }, contentType = { "entry" }) { item ->
        EntryRow(item, today, onComplete, onOpen, Modifier.animateItem())
    }
}
