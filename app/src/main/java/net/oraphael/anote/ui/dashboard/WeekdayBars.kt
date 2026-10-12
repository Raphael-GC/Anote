package net.oraphael.anote.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.DayOfWeek
import java.time.format.TextStyle

private val chartHeight = 96.dp

// Barras verticais por dia da semana (segunda a domingo), uma série só.
// `max` é a escala comum a todos os gráficos lado a lado.
@Composable
fun WeekdayBars(title: String, counts: List<Int>, max: Int, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    val days = DayOfWeek.entries.map { it.getDisplayName(TextStyle.SHORT, AppLocale).removeSuffix(".") }
    val description = title + ": " + days.zip(counts) { day, n -> "$day $n" }.joinToString(", ")
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth(),
            ) {
                counts.forEachIndexed { i, count ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        Box(Modifier.height(chartHeight), contentAlignment = Alignment.BottomCenter) {
                            val fraction = if (max == 0) 0f else count / max.toFloat()
                            if (count > 0) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(chartHeight * fraction)
                                        .background(colors.primary, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                )
                            } else {
                                Spacer(Modifier.height(1.dp))
                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.outline)
                        )
                        Text(days[i], style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
