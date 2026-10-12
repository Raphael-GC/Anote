package net.oraphael.anote.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDateTime

sealed interface BackupOutcome {
    data object NoFolder : BackupOutcome
    data class Written(val fileName: String) : BackupOutcome
    data object PermissionLost : BackupOutcome
    data object WriteFailed : BackupOutcome
}

// Grava um backup na pasta escolhida e mantém só os últimos arquivos.
class AutoBackup(
    private val context: Context,
    private val backupRepository: BackupRepository,
    private val backupFolder: BackupFolder,
    private val statusRepository: BackupStatusRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val resolver get() = context.contentResolver

    suspend fun run(folderUri: Uri?): BackupOutcome = withContext(Dispatchers.IO) {
        if (folderUri == null) return@withContext BackupOutcome.NoFolder
        if (!backupFolder.hasPermission(folderUri)) return@withContext BackupOutcome.PermissionLost
        val fileName = backupFileName(LocalDateTime.now(clock))
        try {
            val folderDoc = DocumentsContract.buildDocumentUriUsingTree(
                folderUri, DocumentsContract.getTreeDocumentId(folderUri)
            )
            val file = DocumentsContract.createDocument(resolver, folderDoc, "application/json", fileName)
                ?: return@withContext BackupOutcome.WriteFailed
            val output = resolver.openOutputStream(file, "wt") ?: return@withContext BackupOutcome.WriteFailed
            backupRepository.writeTo(output)
            deleteOldBackups(folderUri)
            statusRepository.recordBackup(clock.instant(), fileName)
            BackupOutcome.Written(fileName)
        } catch (e: SecurityException) {
            BackupOutcome.PermissionLost
        } catch (e: Exception) {
            // Provedor removido, pasta apagada ou armazenamento indisponível.
            BackupOutcome.WriteFailed
        }
    }

    private fun deleteOldBackups(folderUri: Uri) {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri, DocumentsContract.getTreeDocumentId(folderUri)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
        val idsByName = resolver.query(children, projection, null, null, null)?.use { cursor ->
            buildMap<String, String> { while (cursor.moveToNext()) put(cursor.getString(1), cursor.getString(0)) }
        } ?: return
        backupsToDelete(idsByName.keys.toList()).forEach { name ->
            DocumentsContract.deleteDocument(
                resolver, DocumentsContract.buildDocumentUriUsingTree(folderUri, idsByName.getValue(name))
            )
        }
    }
}
