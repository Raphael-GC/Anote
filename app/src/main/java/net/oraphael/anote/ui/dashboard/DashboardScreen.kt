package net.oraphael.anote.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.MonthStats
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthTitle = DateTimeFormatter.ofPattern("LLLL 'de' yyyy", AppLocale)

fun formatMonth(month: YearMonth): String =
    monthTitle.format(month).replaceFirstChar { it.titlecase(AppLocale) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(app: AnoteApp, onClose: () -> Unit) {
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    val stats by produceState<MonthStats?>(null, month) { value = app.dashboardRepository.stats(month) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
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
            Text(formatMonth(month), style = MaterialTheme.typography.headlineSmall)
            val current = stats ?: return@Column
            StatTile(stringResource(R.string.dashboard_completed), current.completed.toString())
            AreaBars(current.byArea, current.completed)
            StatTile(
                label = stringResource(R.string.dashboard_focus_days),
                value = current.focusDaysFulfilled.toString(),
                detail = pluralStringResource(R.plurals.dashboard_focus_days_detail, current.focusDays, current.focusDays),
            )
            StatTile(
                label = stringResource(R.string.dashboard_extras),
                value = current.extras.toString(),
                detail = stringResource(R.string.dashboard_extras_detail),
            )
            // Pequenos múltiplos com a mesma escala, em vez de duas séries coloridas.
            val max = (current.addedByWeekday + current.completedByWeekday).maxOrNull() ?: 0
            WeekdayBars(stringResource(R.string.dashboard_added_by_weekday), current.addedByWeekday, max)
            WeekdayBars(stringResource(R.string.dashboard_completed_by_weekday), current.completedByWeekday, max)
        }
    }
}

// Número em destaque com seu rótulo; lido como uma frase só pelo leitor de tela.
@Composable
fun StatTile(label: String, value: String, detail: String? = null, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    val description = listOfNotNull(label, value, detail).joinToString(": ")
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
        }
    }
}
