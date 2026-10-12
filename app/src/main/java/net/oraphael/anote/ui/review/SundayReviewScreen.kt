package net.oraphael.anote.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.Week
import net.oraphael.anote.data.WeekSummary
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.rememberToday
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.format.DateTimeFormatter

private val dayMonth = DateTimeFormatter.ofPattern("d 'de' MMM", AppLocale)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SundayReviewScreen(app: AnoteApp, onClose: () -> Unit) {
    val today by rememberToday()
    val summary by produceState<WeekSummary?>(null, today) {
        value = app.weekRepository.summary(Week.containing(today))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_sunday_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            summary?.let { WeekSummaryCard(it) }
        }
    }
}

@Composable
private fun WeekSummaryCard(summary: WeekSummary) {
    val colors = LocalColorTokens.current
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Text(
                text = stringResource(
                    R.string.week_range,
                    dayMonth.format(summary.week.start),
                    dayMonth.format(summary.week.end),
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            SummaryLine(stringResource(R.string.week_completed), summary.completed.toString())
            SummaryLine(stringResource(R.string.week_added), summary.added.toString())
            SummaryLine(
                stringResource(R.string.week_focuses),
                stringResource(R.string.week_focuses_value, summary.focusesCompleted, summary.focusesTotal),
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}
