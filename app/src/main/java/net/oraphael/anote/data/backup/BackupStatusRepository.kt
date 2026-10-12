package net.oraphael.anote.data.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

// Backup (seção 5): quando foi o último backup automático e o último export.
data class BackupStatus(
    val lastBackupAt: Instant? = null,
    val lastExportAt: Instant? = null,
)

class BackupStatusRepository(private val dataStore: DataStore<Preferences>) {

    val status: Flow<BackupStatus> = dataStore.data.map { prefs ->
        BackupStatus(
            lastBackupAt = prefs[LAST_BACKUP_AT]?.let(Instant::ofEpochMilli),
            lastExportAt = prefs[LAST_EXPORT_AT]?.let(Instant::ofEpochMilli),
        )
    }

    suspend fun recordExport(at: Instant) = dataStore.edit { it[LAST_EXPORT_AT] = at.toEpochMilli() }

    private companion object {
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
        val LAST_EXPORT_AT = longPreferencesKey("last_export_at")
    }
}
