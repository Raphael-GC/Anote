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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.UserPreferences
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
        }
    }
}
