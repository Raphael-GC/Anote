package net.oraphael.anote.ui.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.data.backup.BackupStatus
import net.oraphael.anote.ui.formatDateTime
import net.oraphael.anote.ui.theme.Spacing

// Último arquivo de backup gravado e último export.
@Composable
fun BackupStatusText(status: BackupStatus, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        val lastBackupAt = status.lastBackupAt
        val lastBackupFile = status.lastBackupFile
        Text(
            text = if (lastBackupAt != null && lastBackupFile != null) {
                stringResource(R.string.backup_status_last_backup, lastBackupFile, formatDateTime(lastBackupAt))
            } else {
                stringResource(R.string.backup_status_no_backup)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = status.lastExportAt?.let { stringResource(R.string.backup_status_last_export, formatDateTime(it)) }
                ?: stringResource(R.string.backup_status_no_export),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
