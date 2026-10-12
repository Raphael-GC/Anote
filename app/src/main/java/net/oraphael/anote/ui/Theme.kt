package net.oraphael.anote.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import net.oraphael.anote.data.prefs.AppTheme
import net.oraphael.anote.data.prefs.DarkMode
import net.oraphael.anote.ui.theme.ColorTokens
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.LocalShapeTokens
import net.oraphael.anote.ui.theme.ShapeTokens
import net.oraphael.anote.ui.theme.ThemeTokens
import net.oraphael.anote.ui.theme.TypeTokens
import net.oraphael.anote.ui.theme.tokensFor

// Sem cores dinâmicas (Material You), conforme a seção 3.
@Composable
fun AnoteTheme(
    theme: AppTheme = AppTheme.AUTUMN_NOTEBOOK,
    darkMode: DarkMode = DarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    AnoteTheme(tokensFor(theme), darkMode.isDark(), content)
}

@Composable
fun DarkMode.isDark(): Boolean = when (this) {
    DarkMode.SYSTEM -> isSystemInDarkTheme()
    DarkMode.LIGHT -> false
    DarkMode.DARK -> true
}

@Composable
fun AnoteTheme(tokens: ThemeTokens, dark: Boolean, content: @Composable () -> Unit) {
    val colors = (if (dark) tokens.dark else tokens.light).animated()
    CompositionLocalProvider(
        LocalColorTokens provides colors,
        LocalShapeTokens provides tokens.shapes,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(dark),
            shapes = tokens.shapes.toShapes(),
            typography = tokens.type.toTypography(),
            content = content
        )
    }
}

// Troca de tema ou de modo claro/escuro com transição suave de cores.
@Composable
private fun ColorTokens.animated(): ColorTokens {
    @Composable
    fun Color.animate() = animateColorAsState(this, tween(durationMillis = 450), label = "theme").value
    return ColorTokens(
        background = background.animate(),
        surface = surface.animate(),
        surfaceVariant = surfaceVariant.animate(),
        onSurface = onSurface.animate(),
        onSurfaceVariant = onSurfaceVariant.animate(),
        primary = primary.animate(),
        onPrimary = onPrimary.animate(),
        primaryContainer = primaryContainer.animate(),
        onPrimaryContainer = onPrimaryContainer.animate(),
        secondary = secondary.animate(),
        onSecondary = onSecondary.animate(),
        secondaryContainer = secondaryContainer.animate(),
        onSecondaryContainer = onSecondaryContainer.animate(),
        outline = outline.animate(),
        overdue = overdue.animate(),
        dueToday = dueToday.animate(),
        dueSoon = dueSoon.animate(),
    )
}

private fun ColorTokens.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = secondary,
        onTertiary = onSecondary,
        background = background,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerHighest = surfaceVariant,
        outline = outline,
        outlineVariant = outline.copy(alpha = 0.5f),
        error = overdue,
    )
}

private fun ShapeTokens.toShapes() = Shapes(
    extraSmall = RoundedCornerShape(small / 2),
    small = RoundedCornerShape(small),
    medium = RoundedCornerShape(medium),
    large = RoundedCornerShape(large),
    extraLarge = RoundedCornerShape(large * 1.5f),
)

private fun TypeTokens.toTypography(): Typography {
    val base = Typography()
    fun TextStyle.heading() = copy(fontFamily = headingFamily, fontWeight = headingWeight)
    fun TextStyle.body() = copy(fontFamily = bodyFamily)
    return base.copy(
        displayLarge = base.displayLarge.heading(),
        displayMedium = base.displayMedium.heading(),
        displaySmall = base.displaySmall.heading(),
        headlineLarge = base.headlineLarge.heading(),
        headlineMedium = base.headlineMedium.heading(),
        headlineSmall = base.headlineSmall.heading(),
        titleLarge = base.titleLarge.heading(),
        titleMedium = base.titleMedium.heading(),
        titleSmall = base.titleSmall.heading(),
        bodyLarge = base.bodyLarge.body(),
        bodyMedium = base.bodyMedium.body(),
        bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.body(),
        labelMedium = base.labelMedium.body(),
        labelSmall = base.labelSmall.body(),
    )
}
