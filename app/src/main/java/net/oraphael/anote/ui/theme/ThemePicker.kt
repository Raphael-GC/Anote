package net.oraphael.anote.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.AppTheme
import net.oraphael.anote.ui.AnoteTheme

@StringRes
fun AppTheme.labelRes(): Int = when (this) {
    AppTheme.AUTUMN_NOTEBOOK -> R.string.theme_autumn_notebook
    AppTheme.NIGHT_COZY -> R.string.theme_night_cozy
    AppTheme.POCKET_GARDEN -> R.string.theme_pocket_garden
}

// Prévia dos três temas, cada uma desenhada com os próprios tokens.
@Composable
fun ThemePicker(
    selected: AppTheme,
    dark: Boolean,
    onSelect: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        AppTheme.entries.forEach { theme ->
            ThemePreviewCard(
                theme = theme,
                dark = dark,
                selected = theme == selected,
                onClick = { onSelect(theme) },
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(theme: AppTheme, dark: Boolean, selected: Boolean, onClick: () -> Unit) {
    AnoteTheme(tokensFor(theme), dark) {
        val colors = LocalColorTokens.current
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.background,
            border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline),
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        ) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(theme.labelRes()),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    RadioButton(selected = selected, onClick = null)
                }
                PreviewTask()
                PreviewNote()
            }
        }
    }
}

@Composable
private fun PreviewTask() {
    val colors = LocalColorTokens.current
    Surface(shape = MaterialTheme.shapes.medium, color = colors.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Box(
                Modifier
                    .size(18.dp)
                    .border(2.dp, colors.primary, CircleShape)
            )
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.theme_preview_task), style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    Text(
                        text = stringResource(R.string.theme_preview_area),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSecondaryContainer,
                        modifier = Modifier
                            .background(colors.secondaryContainer, MaterialTheme.shapes.small)
                            .padding(horizontal = Spacing.s, vertical = 2.dp),
                    )
                    Text(
                        text = stringResource(R.string.theme_preview_due_today),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.dueToday,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewNote() {
    val colors = LocalColorTokens.current
    Surface(shape = MaterialTheme.shapes.medium, color = colors.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.theme_preview_note),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.m),
        )
    }
}
