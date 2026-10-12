package net.oraphael.anote

import android.app.Application
import net.oraphael.anote.data.backup.BackupRepository
import net.oraphael.anote.data.backup.BackupStatusRepository
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.prefs.PreferencesRepository
import net.oraphael.anote.data.prefs.preferencesDataStore

// Dependências do app, criadas sob demanda.
class AnoteApp : Application() {
    val database by lazy { AnoteDatabase.create(this) }
    val preferencesRepository by lazy { PreferencesRepository(preferencesDataStore) }
    val backupRepository by lazy { BackupRepository(database) }
    val backupStatusRepository by lazy { BackupStatusRepository(preferencesDataStore) }
}
