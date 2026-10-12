package net.oraphael.anote.ui.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.oraphael.anote.R
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

// Página de caderno em branco, nas cores do tema.
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.l),
    ) {
        Canvas(Modifier.size(width = 120.dp, height = 150.dp)) {
            val page = Size(size.width * 0.72f, size.height * 0.82f)
            val topLeft = Offset((size.width - page.width) / 2f, (size.height - page.height) / 2f)
            val radius = CornerRadius(8.dp.toPx())
            rotate(-6f) {
                drawRoundRect(colors.surface, topLeft, page, radius)
                drawRoundRect(colors.outline, topLeft, page, radius, style = Stroke(1.5.dp.toPx()))
                val spacing = page.height / 8f
                for (i in 2..7) {
                    val y = topLeft.y + spacing * i
                    drawLine(
                        colors.outline.copy(alpha = 0.6f),
                        Offset(topLeft.x + 10.dp.toPx(), y),
                        Offset(topLeft.x + page.width - 10.dp.toPx(), y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.list_empty_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.list_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
