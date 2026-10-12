package net.oraphael.anote.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.oraphael.anote.R
import kotlin.math.PI
import kotlin.math.sin

// Abertura animada: a página pautada se enche de anotações (ondulações sobre
// cada pauta) e o nome "Anote!" é escrito em seguida. A geometria segue
// docs/marca/anote-logo.svg (viewBox 144 x 90).

private val Lavender = Color(0xFFB4A7D6)
private val Peach = Color(0xFFF9CB9C)
private val RuleGray = Color(0xFF999999)
private val Ink = Color(0xFF000000)

private const val VIEW_W = 144f
private const val VIEW_H = 90f

private const val PAGE_W = 36f
private const val PAGE_H = 52f
private const val PAGE_RADIUS = 4.5f
private const val PAGE_CX = 115f
private const val PAGE_CY = 38f
private const val PAGE_ROTATION = 25.8f

private const val RULE_X0 = 2.5f
private const val RULE_X1 = 33.5f
private val RULE_YS = List(9) { 9f + it * 4.6f }

// Comprimento de cada linha de anotação, como fração da pauta.
private val NOTE_LENGTHS = listOf(0.80f, 0.95f, 0.70f, 0.90f, 0.55f, 0.88f, 0.75f, 0.92f, 0.45f)
private const val WAVE_LENGTH = 3.8f
private const val WAVE_AMPLITUDE = 0.95f
private const val WAVE_LIFT = 1.7f

private val A_STROKES = listOf(
    "M14 56 C19 42 25 24 31 10 C36 26 41 42 47 56",
    "M22 41.5 C28 40 34 40 40.5 40.8",
)
private val NOTE_STROKES = listOf(
    "M18 74 C18 69 18.2 63 18 58.5",
    "M18.2 65 C21 58 33.5 55.5 35 63.5 C35.6 67.5 35.8 71 36 74",
    "M52 58 C46 57.6 43 62.5 43 66.5 C43 71.5 46.8 74.2 51 74 C55.8 73.8 59 70 59 65.8 C59 61 56 58.2 51 58.2",
    "M70 49 C69.6 57 69 65 69.8 71 C70.3 74 73 74.6 76.5 72.8",
    "M63.5 59 C68 58.6 72.5 58.2 77.5 57.8",
    "M84 66.2 C88.5 66 93.5 65.6 98.2 65 C98.4 60.5 95.3 58 91 58 C86 58 83 62 83 66.5 C83 71.2 86.8 74.2 91 74 C94.2 73.9 96.8 72.4 99 69.8",
    "M106.5 49.5 C106.1 55 105.6 60.5 105.2 66",
    "M104.8 73.2 L104.85 73.3",
)

// Linha do tempo, em milissegundos.
private const val PAGE_IN_MS = 350f
private const val NOTES_START_MS = 250f
private const val NOTE_STAGGER_MS = 80f
private const val NOTE_DRAW_MS = 220f
private const val WORD_START_MS = 1000f
private const val LETTER_DRAW_MS = 110f
private const val HOLD_MS = 350L

private class AnimatedStroke(val path: Path, val color: Color, val width: Float) {
    private val measure = PathMeasure().apply { setPath(path, false) }
    private val length = measure.length
    private val partial = Path()

    fun draw(scope: DrawScope, progress: Float) {
        if (progress <= 0f) return
        val target = if (progress >= 1f) {
            path
        } else {
            partial.reset()
            measure.getSegment(0f, length * progress, partial, true)
            partial
        }
        scope.drawPath(
            target,
            color,
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun wavePath(ruleY: Float, fraction: Float): Path {
    val x0 = RULE_X0 + 0.8f
    val x1 = x0 + (RULE_X1 - RULE_X0 - 1.6f) * fraction
    return Path().apply {
        var x = x0
        moveTo(x, ruleY - WAVE_LIFT)
        while (x < x1) {
            x = (x + 0.25f).coerceAtMost(x1)
            val y = ruleY - WAVE_LIFT -
                WAVE_AMPLITUDE * sin((x - x0) / WAVE_LENGTH * 2f * PI.toFloat())
            lineTo(x, y)
        }
    }
}

private fun parse(d: String): Path = PathParser().parsePathString(d).toPath()

private fun phase(timeMs: Float, startMs: Float, durationMs: Float): Float =
    FastOutSlowInEasing.transform(((timeMs - startMs) / durationMs).coerceIn(0f, 1f))

private val TOTAL_MS = WORD_START_MS + (A_STROKES.size + NOTE_STROKES.size) * LETTER_DRAW_MS

@Composable
fun AnoteSplash(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    val clock = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        clock.animateTo(TOTAL_MS, tween(TOTAL_MS.toInt(), easing = LinearEasing))
        delay(HOLD_MS)
        currentOnFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.splash_background)),
        contentAlignment = Alignment.Center
    ) {
        AnoteLogo(
            timeMs = { clock.value },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}

// Logo desenhado no instante `timeMs` da animação; sem tempo, o quadro final.
@Composable
fun AnoteLogo(modifier: Modifier = Modifier, timeMs: () -> Float = { TOTAL_MS }) {
    val notes = remember {
        RULE_YS.zip(NOTE_LENGTHS) { y, f -> AnimatedStroke(wavePath(y, f), Ink, 0.7f) }
    }
    val letters = remember {
        A_STROKES.map { AnimatedStroke(parse(it), Lavender, 3.8f) } +
            NOTE_STROKES.map { AnimatedStroke(parse(it), Peach, 3.5f) }
    }
    val description = stringResource(R.string.app_name)
    Canvas(
        modifier = modifier
            .aspectRatio(VIEW_W / VIEW_H)
            .semantics { contentDescription = description }
    ) {
        val t = timeMs()
        val unit = size.width / VIEW_W
        scale(unit, unit, pivot = Offset.Zero) {
            val pageIn = phase(t, 0f, PAGE_IN_MS)
            rotate(PAGE_ROTATION - 8f * (1f - pageIn), pivot = Offset(PAGE_CX, PAGE_CY)) {
                scale(0.9f + 0.1f * pageIn, pivot = Offset(PAGE_CX, PAGE_CY)) {
                    translate(PAGE_CX - PAGE_W / 2f, PAGE_CY - PAGE_H / 2f) {
                        drawPage(alpha = pageIn)
                        notes.forEachIndexed { i, note ->
                            note.draw(this, phase(t, NOTES_START_MS + i * NOTE_STAGGER_MS, NOTE_DRAW_MS))
                        }
                    }
                }
            }
            letters.forEachIndexed { i, letter ->
                letter.draw(this, phase(t, WORD_START_MS + i * LETTER_DRAW_MS, LETTER_DRAW_MS))
            }
        }
    }
}

private fun DrawScope.drawPage(alpha: Float) {
    val radius = CornerRadius(PAGE_RADIUS)
    val pageSize = Size(PAGE_W, PAGE_H)
    drawRoundRect(Color.White, size = pageSize, cornerRadius = radius, alpha = alpha)
    drawRoundRect(RuleGray, size = pageSize, cornerRadius = radius, alpha = alpha, style = Stroke(1f))
    RULE_YS.forEach { y ->
        drawLine(RuleGray, Offset(RULE_X0, y), Offset(RULE_X1, y), strokeWidth = 0.7f, alpha = alpha)
    }
}
