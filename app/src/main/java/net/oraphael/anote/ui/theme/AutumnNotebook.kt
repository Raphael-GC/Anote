package net.oraphael.anote.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Caderno de Outono: papel creme, tinta marrom, ferrugem e mostarda.
val AutumnNotebookTokens = ThemeTokens(
    light = ColorTokens(
        background = Color(0xFFF6EEDF),
        surface = Color(0xFFFBF6EC),
        surfaceVariant = Color(0xFFEDE1CB),
        onSurface = Color(0xFF3B2A20),
        onSurfaceVariant = Color(0xFF6B5646),
        primary = Color(0xFF9A4A2B),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF2D2C0),
        onPrimaryContainer = Color(0xFF3E1A0B),
        secondary = Color(0xFF8A6514),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3E2B4),
        onSecondaryContainer = Color(0xFF3A2A00),
        outline = Color(0xFF958069),
        overdue = Color(0xFFA3291D),
        dueToday = Color(0xFF955300),
        dueSoon = Color(0xFF56652A),
    ),
    dark = ColorTokens(
        background = Color(0xFF1F1813),
        surface = Color(0xFF2A211A),
        surfaceVariant = Color(0xFF3A2E24),
        onSurface = Color(0xFFEFE3D1),
        onSurfaceVariant = Color(0xFFC7B49E),
        primary = Color(0xFFE8A07E),
        onPrimary = Color(0xFF4A1E0A),
        primaryContainer = Color(0xFF6E3219),
        onPrimaryContainer = Color(0xFFFFDBCB),
        secondary = Color(0xFFE2C06A),
        onSecondary = Color(0xFF3A2A00),
        secondaryContainer = Color(0xFF5A4410),
        onSecondaryContainer = Color(0xFFFFE7A8),
        outline = Color(0xFF8A7562),
        overdue = Color(0xFFF2958A),
        dueToday = Color(0xFFF0B866),
        dueSoon = Color(0xFFB9C786),
    ),
    shapes = ShapeTokens(small = 4.dp, medium = 8.dp, large = 12.dp),
    type = TypeTokens(
        headingFamily = FontFamily.Serif,
        bodyFamily = FontFamily.Serif,
        headingWeight = FontWeight.Medium,
    ),
)
