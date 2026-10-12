package net.oraphael.anote.ui.entry

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.ui.AppIcons

// Abre a captura rápida; presente em todas as telas principais.
@Composable
fun CaptureButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(AppIcons.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.capture_open)) },
    )
}
