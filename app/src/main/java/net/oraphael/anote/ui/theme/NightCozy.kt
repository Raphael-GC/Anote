package net.oraphael.anote.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Aconchego Noturno: lavanda acinzentada, índigo e o âmbar de um abajur.
val NightCozyTokens = ThemeTokens(
    light = ColorTokens(
        background = Color(0xFFF3EFF6),
        surface = Color(0xFFFBF9FC),
        surfaceVariant = Color(0xFFE4DEEB),
        onSurface = Color(0xFF2A2433),
        onSurfaceVariant = Color(0xFF5E566B),
        primary = Color(0xFF5B4A8B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE3DBFA),
        onPrimaryContainer = Color(0xFF1D1240),
        secondary = Color(0xFF94591A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFBE3C4),
        onSecondaryContainer = Color(0xFF3A2105),
        outline = Color(0xFF8A8296),
        overdue = Color(0xFFB3364A),
        dueToday = Color(0xFF85500A),
        dueSoon = Color(0xFF3A6370),
    ),
    dark = ColorTokens(
        background = Color(0xFF16131D),
        surface = Color(0xFF1F1B28),
        surfaceVariant = Color(0xFF2E2939),
        onSurface = Color(0xFFECE6F3),
        onSurfaceVariant = Color(0xFFBDB4CB),
        primary = Color(0xFFC6B8F5),
        onPrimary = Color(0xFF2D2160),
        primaryContainer = Color(0xFF43367A),
        onPrimaryContainer = Color(0xFFE6DEFF),
        secondary = Color(0xFFF2B66B),
        onSecondary = Color(0xFF462A00),
        secondaryContainer = Color(0xFF5E3E10),
        onSecondaryContainer = Color(0xFFFFDDB3),
        outline = Color(0xFF7D748C),
        overdue = Color(0xFFFF9AA8),
        dueToday = Color(0xFFF7C072),
        dueSoon = Color(0xFF8FC3D1),
    ),
    shapes = ShapeTokens(small = 12.dp, medium = 16.dp, large = 24.dp),
    type = TypeTokens(
        headingFamily = FontFamily.SansSerif,
        bodyFamily = FontFamily.SansSerif,
        headingWeight = FontWeight.Normal,
    ),
)
