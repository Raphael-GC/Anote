package net.oraphael.anote.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.BackupChoice
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.backup.rememberBackupFolderPicker
import net.oraphael.anote.ui.theme.LocalColorTokens

// Resposta obrigatória: escolher a pasta agora ou seguir sem backup depois de
// confirmar. Só avança com uma das duas.
@Composable
fun BackupStep(app: AnoteApp, preferences: UserPreferences, nextLabel: String, onNext: () -> Unit) {
    val scope = rememberCoroutineScope()
    var confirmSkip by rememberSaveable { mutableStateOf(false) }
    var folderFailed by rememberSaveable { mutableStateOf(false) }
    val chooseFolder = rememberBackupFolderPicker(app) { folderFailed = true }
    val answered = preferences.backupChoice != BackupChoice.UNANSWERED
    val folderName = remember(preferences.backupFolderUri) {
        preferences.backupFolderUri?.let { app.backupFolder.displayName(it.toUri()) ?: it }
    }

    OnboardingPage(
        title = stringResource(R.string.onboarding_backup_title),
        primaryLabel = nextLabel,
        primaryEnabled = answered,
        onPrimary = onNext,
    ) {
        Text(stringResource(R.string.onboarding_backup_body), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = chooseFolder, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_backup_choose))
        }
        OutlinedButton(onClick = { confirmSkip = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_backup_skip))
        }
        val status = when {
            folderFailed -> stringResource(R.string.backup_folder_failed)
            preferences.backupChoice == BackupChoice.FOLDER && folderName != null ->
                stringResource(R.string.onboarding_backup_chosen, folderName)
            preferences.backupChoice == BackupChoice.NO_BACKUP -> stringResource(R.string.onboarding_backup_skipped)
            else -> stringResource(R.string.onboarding_backup_required)
        }
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = if (folderFailed || preferences.backupChoice == BackupChoice.NO_BACKUP) {
                LocalColorTokens.current.overdue
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }

    if (confirmSkip) {
        AlertDialog(
            onDismissRequest = { confirmSkip = false },
            title = { Text(stringResource(R.string.onboarding_backup_skip_title)) },
            text = { Text(stringResource(R.string.onboarding_backup_skip_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmSkip = false
                    folderFailed = false
                    scope.launch { app.preferencesRepository.setBackupChoice(BackupChoice.NO_BACKUP) }
                }) { Text(stringResource(R.string.onboarding_backup_skip_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmSkip = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
