package net.oraphael.anote.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

// Pasta de backup escolhida pelo usuário (Storage Access Framework). A permissão
// é persistida para valer depois de reiniciar o aparelho.
class BackupFolder(private val context: Context) {
    private val resolver get() = context.contentResolver

    fun takePermission(uri: Uri) {
        resolver.persistedUriPermissions
            .filter { it.uri != uri }
            .forEach { resolver.releasePersistableUriPermission(it.uri, FLAGS) }
        resolver.takePersistableUriPermission(uri, FLAGS)
    }

    fun hasPermission(uri: Uri): Boolean =
        resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission && it.isWritePermission }

    fun displayName(uri: Uri): String? = runCatching {
        val document = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        resolver.query(document, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
    }.getOrNull()

    private companion object {
        const val FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}
