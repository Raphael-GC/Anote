package net.oraphael.anote.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

// Ícones desenhados aqui, sem depender da biblioteca de ícones do Material.
object AppIcons {
    val Close: ImageVector by lazy {
        icon("M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12z")
    }

    private fun icon(path: String) = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(PathParser().parsePathString(path).toNodes(), fill = SolidColor(Color.Black))
        .build()
}
