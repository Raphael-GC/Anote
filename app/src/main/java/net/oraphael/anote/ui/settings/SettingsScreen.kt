package net.oraphael.anote.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import net.oraphael.anote.data.backup.BackupStatus
import net.oraphael.anote.data.backup.preview
import net.oraphael.anote.data.prefs.DarkMode
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.NotificationSwitch
import net.oraphael.anote.ui.isDark
import net.oraphael.anote.ui.onboarding.ReviewTimesEditor
import net.oraphael.anote.ui.backup.BackupStatusText
import net.oraphael.anote.ui.backup.BackupWarning
import net.oraphael.anote.ui.backup.ImportDialog
import net.oraphael.anote.ui.backup.rememberExportLauncher
import net.oraphael.anote.ui.backup.rememberBackupFolderPicker
import net.oraphael.anote.ui.entry.CaptureButton
import net.oraphael.anote.ui.backup.messageRes
import net.oraphael.anote.ui.theme.Spacing
import net.oraphael.anote.ui.theme.ThemePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(app: AnoteApp, preferences: UserPreferences, onClose: () -> Unit, onCapture: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val exportDone = stringResource(R.string.export_done)
    val exportFailed = stringResource(R.string.export_failed)
    val importDone = stringResource(R.string.import_done)
    val resources = context.resources
    var pendingImport by remember { mutableStateOf<BackupData?>(null) }
    val backupStatus by app.backupStatusRepository.status.collectAsState(initial = BackupStatus())

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
    val chooseFolder = rememberBackupFolderPicker(app) { snackbar.showSnackbar(folderFailed) }
    val folderName = remember(preferences.backupFolderUri) {
        preferences.backupFolderUri?.let { app.backupFolder.displayName(it.toUri()) ?: it }
    }

    val export = rememberExportLauncher(app) { ok -> snackbar.showSnackbar(if (ok) exportDone else exportFailed) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(AppIcons.Back, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = { CaptureButton(onCapture) },
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
            DarkModeSelector(
                selected = preferences.darkMode,
                onSelect = { scope.launch { app.preferencesRepository.setDarkMode(it) } },
            )
            Text(
                text = stringResource(R.string.settings_reviews_title),
                style = MaterialTheme.typography.headlineMedium
            )
            ReviewTimesEditor(app, preferences)
            Text(
                text = stringResource(R.string.notifications_section_title),
                style = MaterialTheme.typography.headlineMedium
            )
            NotificationSwitch(
                title = stringResource(R.string.notifications_reminders),
                description = stringResource(R.string.notifications_reminders_hint),
                checked = preferences.remindersEnabled,
                onCheckedChange = { scope.launch { app.preferencesRepository.setRemindersEnabled(it) } },
            )
            NotificationSwitch(
                title = stringResource(R.string.notifications_due),
                description = stringResource(R.string.notifications_due_hint),
                checked = preferences.dueNoticeEnabled,
                onCheckedChange = { scope.launch { app.preferencesRepository.setDueNoticeEnabled(it) } },
            )
            Text(
                text = stringResource(R.string.data_section_title),
                style = MaterialTheme.typography.headlineMedium
            )
            backupStatus.problem?.let { problem ->
                BackupWarning(problem = problem, onChooseFolder = chooseFolder)
            }
            BackupStatusText(backupStatus)
            OutlinedButton(onClick = export) {
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
            OutlinedButton(onClick = chooseFolder) {
                Text(
                    stringResource(
                        if (folderName == null) R.string.backup_folder_choose else R.string.backup_folder_change
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkModeSelector(selected: DarkMode, onSelect: (DarkMode) -> Unit) {
    val options = listOf(
        DarkMode.SYSTEM to R.string.dark_mode_system,
        DarkMode.LIGHT to R.string.dark_mode_light,
        DarkMode.DARK to R.string.dark_mode_dark,
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(stringResource(label)) }
        }
    }
}
