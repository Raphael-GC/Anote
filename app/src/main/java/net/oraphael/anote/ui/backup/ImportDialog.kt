package net.oraphael.anote.ui.backup

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import net.oraphael.anote.R
import net.oraphael.anote.data.backup.BackupFormatException
import net.oraphael.anote.data.backup.ImportMode
import net.oraphael.anote.data.backup.ImportPreview
import net.oraphael.anote.ui.formatDateTime
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

@StringRes
fun BackupFormatException.Reason.messageRes(): Int = when (this) {
    BackupFormatException.Reason.NOT_A_BACKUP -> R.string.import_error_not_backup
    BackupFormatException.Reason.NEWER_VERSION -> R.string.import_error_newer
    BackupFormatException.Reason.UNSUPPORTED_VERSION -> R.string.import_error_unsupported
    BackupFormatException.Reason.MISSING_VERSION,
    BackupFormatException.Reason.CORRUPTED -> R.string.import_error_corrupted
}

@Composable
fun ImportDialog(
    preview: ImportPreview,
    onConfirm: (ImportMode) -> Unit,
    onDismiss: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(ImportMode.MERGE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_preview_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(stringResource(R.string.import_preview_exported_at, formatDateTime(preview.exportedAt)))
                Text(stringResource(R.string.import_preview_tasks, preview.tasks))
                Text(stringResource(R.string.import_preview_notes, preview.notes))
                Text(stringResource(R.string.import_preview_areas, preview.areas))
                Text(stringResource(R.string.import_preview_focuses, preview.focuses))
                Column(Modifier.padding(top = Spacing.m).selectableGroup()) {
                    ModeOption(
                        selected = mode == ImportMode.MERGE,
                        label = R.string.import_mode_merge,
                        hint = R.string.import_mode_merge_hint,
                        onClick = { mode = ImportMode.MERGE },
                    )
                    ModeOption(
                        selected = mode == ImportMode.REPLACE,
                        label = R.string.import_mode_replace,
                        hint = R.string.import_mode_replace_hint,
                        warning = true,
                        onClick = { mode = ImportMode.REPLACE },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(mode) }) { Text(stringResource(R.string.import_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun ModeOption(
    selected: Boolean,
    @StringRes label: Int,
    @StringRes hint: Int,
    onClick: () -> Unit,
    warning: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.padding(start = Spacing.s)) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(hint),
                style = MaterialTheme.typography.bodySmall,
                color = if (warning && selected) LocalColorTokens.current.overdue else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
