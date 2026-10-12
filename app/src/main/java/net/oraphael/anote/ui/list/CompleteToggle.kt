package net.oraphael.anote.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.oraphael.anote.ui.theme.LocalColorTokens

const val COMPLETE_ANIMATION_MS = 350

// Círculo que se preenche e desenha um visto ao concluir.
@Composable
fun CompleteToggle(checked: Boolean, onCheck: () -> Unit, description: String, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(COMPLETE_ANIMATION_MS),
        label = "complete",
    )
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .toggleable(value = checked, enabled = !checked, role = Role.Checkbox, onValueChange = { onCheck() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(22.dp)) {
            val stroke = 2.dp.toPx()
            val radius = size.minDimension / 2f - stroke / 2f
            drawCircle(colors.primary, radius = radius * progress)
            drawCircle(colors.primary, radius = radius, style = Stroke(stroke))
            if (progress > 0f) {
                val check = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.68f)
                    lineTo(size.width * 0.74f, size.height * 0.36f)
                }
                val measure = PathMeasure().apply { setPath(check, false) }
                val partial = Path()
                measure.getSegment(0f, measure.length * progress, partial, true)
                drawPath(
                    partial,
                    colors.onPrimary,
                    style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}
