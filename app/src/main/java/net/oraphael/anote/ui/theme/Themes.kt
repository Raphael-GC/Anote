package net.oraphael.anote.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.oraphael.anote.data.prefs.AppTheme

fun tokensFor(theme: AppTheme): ThemeTokens = when (theme) {
    AppTheme.AUTUMN_NOTEBOOK -> ProvisionalTokens
    AppTheme.NIGHT_COZY -> ProvisionalTokens
    AppTheme.POCKET_GARDEN -> ProvisionalTokens
}

// Neutro, até cada tema ganhar suas cores.
private val ProvisionalTokens = ThemeTokens(
    light = ColorTokens(
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurface = Color(0xFF1C1B1F),
        onSurfaceVariant = Color(0xFF49454F),
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        outline = Color(0xFF79747E),
        overdue = Color(0xFFB3261E),
        dueToday = Color(0xFF8C5000),
        dueSoon = Color(0xFF3D6A8A),
    ),
    dark = ColorTokens(
        background = Color(0xFF1C1B1F),
        surface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFF49454F),
        onSurface = Color(0xFFE6E1E5),
        onSurfaceVariant = Color(0xFFCAC4D0),
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        outline = Color(0xFF938F99),
        overdue = Color(0xFFF2B8B5),
        dueToday = Color(0xFFFFB870),
        dueSoon = Color(0xFF9CCBF0),
    ),
    shapes = ShapeTokens(small = 8.dp, medium = 12.dp, large = 16.dp),
    type = TypeTokens(
        headingFamily = FontFamily.SansSerif,
        bodyFamily = FontFamily.SansSerif,
        headingWeight = FontWeight.Normal,
    ),
)
