package net.oraphael.anote.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.backup.BackupData
import net.oraphael.anote.data.backup.BackupFormatException
import net.oraphael.anote.data.backup.BackupWorker
import net.oraphael.anote.data.backup.preview
import net.oraphael.anote.data.prefs.BackupChoice
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.backup.ImportDialog
import net.oraphael.anote.ui.backup.messageRes
import net.oraphael.anote.ui.theme.Spacing
import net.oraphael.anote.ui.theme.ThemePicker
import java.time.Instant
import java.time.LocalDate

// Tela provisória: hospeda o seletor de tema e os controles de dados até o
// onboarding (Fase 4), a lista principal (Fase 5) e as Configurações (Fase 11).
@Composable
fun ProvisionalScreen(app: AnoteApp, preferences: UserPreferences) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val exportDone = stringResource(R.string.export_done)
    val exportFailed = stringResource(R.string.export_failed)
    val importDone = stringResource(R.string.import_done)
    val resources = context.resources
    var pendingImport by remember { mutableStateOf<BackupData?>(null) }

    suspend fun showImportError(e: Throwable) {
        val reason = (e as? BackupFormatException)?.reason ?: BackupFormatException.Reason.CORRUPTED
        snackbar.showSnackbar(resources.getString(reason.messageRes()))
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val input = requireNotNull(context.contentResolver.openInputStream(uri))
                app.backupRepository.readFrom(input)
            }.onSuccess { pendingImport = it }.onFailure { showImportError(it) }
        }
    }

    pendingImport?.let { data ->
        ImportDialog(
            preview = data.preview(),
            onDismiss = { pendingImport = null },
            onConfirm = { mode ->
                pendingImport = null
                scope.launch {
                    runCatching { app.backupRepository.import(data, mode) }
                        .onSuccess { snackbar.showSnackbar(importDone) }
                        .onFailure { showImportError(it) }
                }
            },
        )
    }

    val folderFailed = stringResource(R.string.backup_folder_failed)
    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                app.backupFolder.takePermission(uri)
                app.preferencesRepository.setBackupFolderUri(uri.toString())
                app.preferencesRepository.setBackupChoice(BackupChoice.FOLDER)
                BackupWorker.runNow(context)
            }.onFailure { snackbar.showSnackbar(folderFailed) }
        }
    }
    val folderName = remember(preferences.backupFolderUri) {
        preferences.backupFolderUri?.let { app.backupFolder.displayName(it.toUri()) ?: it }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = runCatching {
                val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
                app.backupRepository.writeTo(output)
                app.backupStatusRepository.recordExport(Instant.now())
            }.isSuccess
            snackbar.showSnackbar(if (ok) exportDone else exportFailed)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            Text(
                text = stringResource(R.string.theme_picker_title),
                style = MaterialTheme.typography.headlineMedium
            )
            ThemePicker(
                selected = preferences.theme,
                dark = preferences.darkMode.isDark(),
                onSelect = { scope.launch { app.preferencesRepository.setTheme(it) } }
            )
            Text(
                text = stringResource(R.string.data_section_title),
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedButton(onClick = { exportLauncher.launch("anote-${LocalDate.now()}.json") }) {
                Text(stringResource(R.string.export_action))
            }
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }) {
                Text(stringResource(R.string.import_action))
            }
            Text(
                text = folderName?.let { stringResource(R.string.backup_folder_current, it) }
                    ?: stringResource(R.string.backup_folder_none),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { folderLauncher.launch(null) }) {
                Text(
                    stringResource(
                        if (folderName == null) R.string.backup_folder_choose else R.string.backup_folder_change
                    )
                )
            }
        }
    }
}
