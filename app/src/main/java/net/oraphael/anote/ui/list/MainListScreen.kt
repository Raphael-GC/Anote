package net.oraphael.anote.ui.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.SortOrder
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.entry.CaptureButton
import net.oraphael.anote.ui.entry.EditEntrySheet
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(app: AnoteApp, onOpenSettings: () -> Unit, onCapture: () -> Unit) {
    val today = remember { LocalDate.now() }
    var order by rememberSaveable { mutableStateOf(SortOrder.OLDEST) }
    val list by remember(today, order) { app.mainListSource.observe(today, order) }.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val complete: (Long) -> Unit = { id -> scope.launch { app.entryRepository.complete(id) } }
    var editing by remember { mutableStateOf<ListEntry?>(null) }
    editing?.let { EditEntrySheet(app, it, onDismiss = { editing = null }) }
    val open: (ListEntry) -> Unit = { editing = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(AppIcons.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
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
