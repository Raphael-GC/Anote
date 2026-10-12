package net.oraphael.anote.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import java.time.Instant
import java.time.LocalDate

// "Salvar como" do sistema para exportar os dados em JSON; registra o export.
@Composable
fun rememberExportLauncher(app: AnoteApp, onResult: suspend (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = runCatching {
                val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
                app.backupRepository.writeTo(output)
                app.backupStatusRepository.recordExport(Instant.now())
            }.isSuccess
            onResult(ok)
        }
    }
    return { launcher.launch("anote-${LocalDate.now()}.json") }
}
