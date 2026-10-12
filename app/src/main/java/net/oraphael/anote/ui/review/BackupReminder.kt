package net.oraphael.anote.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.backup.BackupStatus
import net.oraphael.anote.data.backup.BackupWorker
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.backup.BackupStatusText
import net.oraphael.anote.ui.backup.BackupWarning
import net.oraphael.anote.ui.backup.rememberBackupFolderPicker
import net.oraphael.anote.ui.backup.rememberExportLauncher
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

// Lembrete de backup no ritual de domingo, com atalho para fazer agora.
@Composable
fun BackupReminder(app: AnoteApp, preferences: UserPreferences) {
    val context = LocalContext.current
    val colors = LocalColorTokens.current
    val status by app.backupStatusRepository.status.collectAsState(initial = BackupStatus())
    var message by remember { mutableStateOf<Int?>(null) }
    val export = rememberExportLauncher(app) { ok -> message = if (ok) R.string.export_done else R.string.export_failed }
    val chooseFolder = rememberBackupFolderPicker(app) { message = R.string.backup_folder_failed }
    val hasFolder = preferences.backupFolderUri != null

    Surface(shape = MaterialTheme.shapes.large, color = colors.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(stringResource(R.string.week_backup_title), style = MaterialTheme.typography.titleMedium)
            status.problem?.let { BackupWarning(problem = it, onChooseFolder = chooseFolder) }
            if (!hasFolder) {
                Text(
                    text = stringResource(R.string.week_backup_no_folder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.overdue,
                )
            }
            BackupStatusText(status)
            if (hasFolder) {
                OutlinedButton(
                    onClick = {
                        BackupWorker.runNow(context)
                        message = R.string.week_backup_started
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.week_backup_now)) }
            } else {
                OutlinedButton(onClick = export, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.week_backup_export_now))
                }
                TextButton(onClick = chooseFolder) { Text(stringResource(R.string.backup_folder_choose)) }
            }
            message?.let {
                Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}
