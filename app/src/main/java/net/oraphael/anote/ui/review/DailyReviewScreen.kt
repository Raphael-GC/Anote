package net.oraphael.anote.ui.review

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(app: AnoteApp, onClose: () -> Unit) {
    val pending by remember { app.reviewRepository.pendingTasks() }.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_daily_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        val tasks = pending ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Spacing.l),
        ) {
            item(key = "intro") {
                Text(
                    text = stringResource(R.string.review_daily_intro),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = Spacing.l),
                )
            }
            items(tasks, key = { it.entry.id }) { item -> ReviewRow(item) }
        }
    }
}

@Composable
private fun ReviewRow(item: ListEntry) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = LocalColorTokens.current.surface,
        modifier = Modifier.padding(vertical = Spacing.xs),
    ) {
        Text(
            text = item.entry.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(Spacing.m)
                .fillMaxSize(),
        )
    }
}
