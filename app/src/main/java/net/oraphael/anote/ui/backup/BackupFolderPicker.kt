package net.oraphael.anote.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.data.backup.BackupWorker
import net.oraphael.anote.data.prefs.BackupChoice

// Abre o seletor de pasta do sistema. Com a pasta escolhida: persiste a
// permissão, salva a URI, registra a resposta "pasta" e grava um backup já.
@Composable
fun rememberBackupFolderPicker(app: AnoteApp, onFailure: suspend () -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                app.backupFolder.takePermission(uri)
                app.preferencesRepository.setBackupFolderUri(uri.toString())
                app.preferencesRepository.setBackupChoice(BackupChoice.FOLDER)
                BackupWorker.runNow(context)
            }.onFailure { onFailure() }
        }
    }
    return { launcher.launch(null) }
}
