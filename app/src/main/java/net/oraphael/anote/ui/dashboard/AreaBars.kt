package net.oraphael.anote.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.oraphael.anote.R
import net.oraphael.anote.data.AreaCount
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import kotlin.math.roundToInt

// Barras horizontais de uma série só (cor primária); rótulo e valor escritos em
// cada linha, então a leitura não depende da cor.
@Composable
fun AreaBars(byArea: List<AreaCount>, total: Int) {
    val colors = LocalColorTokens.current
    val noArea = stringResource(R.string.dashboard_no_area)
    val max = byArea.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Text(stringResource(R.string.dashboard_by_area), style = MaterialTheme.typography.titleMedium)
            byArea.forEach { (area, count) ->
                val name = area?.name ?: noArea
                val percent = if (total == 0) 0 else (count * 100f / total).roundToInt()
                val value = stringResource(R.string.dashboard_area_value, count, percent)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                    modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$name: $value" },
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(96.dp),
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(12.dp)
                            .background(colors.surfaceVariant, RoundedCornerShape(4.dp))
                    ) {
                        if (count > 0) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(count / max.toFloat())
                                    .background(colors.primary, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.width(64.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.dashboard_area_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}
