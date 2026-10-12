package net.oraphael.anote.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Tokens de design. Cada tema visual define cores (claro e escuro), formas e
// tipografia; o espaçamento é comum aos três. O tema muda só a estética.

@Immutable
data class ColorTokens(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val outline: Color,
    // Indicadores de prazo: vencido, hoje e próximos dias.
    val overdue: Color,
    val dueToday: Color,
    val dueSoon: Color,
)

@Immutable
data class ShapeTokens(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
)

@Immutable
data class TypeTokens(
    val headingFamily: FontFamily,
    val bodyFamily: FontFamily,
    val headingWeight: FontWeight,
)

@Immutable
data class ThemeTokens(
    val light: ColorTokens,
    val dark: ColorTokens,
    val shapes: ShapeTokens,
    val type: TypeTokens,
)

object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

val LocalColorTokens = staticCompositionLocalOf<ColorTokens> { error("Fora do AnoteTheme") }
val LocalShapeTokens = staticCompositionLocalOf<ShapeTokens> { error("Fora do AnoteTheme") }
