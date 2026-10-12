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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListScreen(app: AnoteApp, onOpenSettings: () -> Unit) {
    val today = remember { LocalDate.now() }
    val list by remember(today) { app.mainListSource.observe(today) }.collectAsState(initial = null)

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
    ) { innerPadding ->
        val current = list ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.l),
        ) {
            section(R.string.list_focuses_title, current.focuses)
            section(R.string.list_tasks_title, current.tasks)
        }
    }
}

private fun LazyListScope.section(title: Int, entries: List<ListEntry>) {
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
        EntryRow(item, Modifier.animateItem())
    }
}
