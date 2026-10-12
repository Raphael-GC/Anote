package net.oraphael.anote.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Jardim de Bolso: folhas, flor de cerejeira e terra. Sem elementos de progresso.
val PocketGardenTokens = ThemeTokens(
    light = ColorTokens(
        background = Color(0xFFF1F5EC),
        surface = Color(0xFFFAFCF7),
        surfaceVariant = Color(0xFFDFE8D6),
        onSurface = Color(0xFF1F2A1E),
        onSurfaceVariant = Color(0xFF52604C),
        primary = Color(0xFF3F6B3A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFCDE8C4),
        onPrimaryContainer = Color(0xFF0B2108),
        secondary = Color(0xFFA54D6B),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF8D9E3),
        onSecondaryContainer = Color(0xFF3D0F20),
        outline = Color(0xFF7F8F77),
        overdue = Color(0xFFB03A2E),
        dueToday = Color(0xFF855500),
        dueSoon = Color(0xFF38627E),
    ),
    dark = ColorTokens(
        background = Color(0xFF121812),
        surface = Color(0xFF1A221A),
        surfaceVariant = Color(0xFF283327),
        onSurface = Color(0xFFE2EBDD),
        onSurfaceVariant = Color(0xFFB7C4B0),
        primary = Color(0xFFA6D49B),
        onPrimary = Color(0xFF133810),
        primaryContainer = Color(0xFF285223),
        onPrimaryContainer = Color(0xFFC9F0BE),
        secondary = Color(0xFFF0A8C0),
        onSecondary = Color(0xFF521D33),
        secondaryContainer = Color(0xFF6E2F48),
        onSecondaryContainer = Color(0xFFFFD9E4),
        outline = Color(0xFF7C8B76),
        overdue = Color(0xFFF49A8F),
        dueToday = Color(0xFFEDC06A),
        dueSoon = Color(0xFF94C4E0),
    ),
    shapes = ShapeTokens(small = 16.dp, medium = 24.dp, large = 32.dp),
    type = TypeTokens(
        headingFamily = FontFamily.SansSerif,
        bodyFamily = FontFamily.SansSerif,
        headingWeight = FontWeight.Medium,
    ),
)
