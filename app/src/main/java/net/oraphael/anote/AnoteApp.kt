package net.oraphael.anote

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.oraphael.anote.data.AreaRepository
import net.oraphael.anote.data.DashboardRepository
import net.oraphael.anote.data.EntryRepository
import net.oraphael.anote.data.FocusRepository
import net.oraphael.anote.data.MainListSource
import net.oraphael.anote.data.ReviewRepository
import net.oraphael.anote.data.WeekRepository
import net.oraphael.anote.data.backup.AutoBackup
import net.oraphael.anote.data.backup.BackupFolder
import net.oraphael.anote.data.backup.BackupRepository
import net.oraphael.anote.data.backup.BackupStatusRepository
import net.oraphael.anote.data.backup.BackupWorker
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.ReviewType
import net.oraphael.anote.data.prefs.PreferencesRepository
import net.oraphael.anote.data.reminders.ReviewReminders
import net.oraphael.anote.data.prefs.preferencesDataStore

// Dependências do app, criadas sob demanda.
class AnoteApp : Application() {
    val database by lazy { AnoteDatabase.create(this) }
    val preferencesRepository by lazy { PreferencesRepository(preferencesDataStore) }
    val areaRepository by lazy { AreaRepository(database.areaDao(), preferencesRepository) }
    val backupRepository by lazy { BackupRepository(database) }
    val entryRepository by lazy {
        EntryRepository(database.entryDao(), database.entryAreaDao(), database.focusDao())
    }
    val mainListSource by lazy { MainListSource(database, preferencesRepository) }
    val focusRepository by lazy { FocusRepository(database.entryDao(), database.focusDao()) }
    val weekRepository by lazy { WeekRepository(database) }
    val dashboardRepository by lazy { DashboardRepository(database) }
    val reviewRepository by lazy { ReviewRepository(database, preferencesRepository, focusRepository) }
    val backupStatusRepository by lazy { BackupStatusRepository(preferencesDataStore) }
    val backupFolder by lazy { BackupFolder(this) }
    val autoBackup by lazy { AutoBackup(this, backupRepository, backupFolder, backupStatusRepository) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        BackupWorker.scheduleDaily(this)
        ReviewReminders.createChannel(this)
        // Reagenda os lembretes das revisões sempre que um horário muda.
        appScope.launch {
            preferencesRepository.preferences
                .map { it.dailyReviewTime }
                .distinctUntilChanged()
                .collect { ReviewReminders.schedule(this@AnoteApp, ReviewType.DAILY, it) }
        }
        appScope.launch {
            preferencesRepository.preferences
                .map { it.sundayReviewTime }
                .distinctUntilChanged()
                .collect { ReviewReminders.schedule(this@AnoteApp, ReviewType.SUNDAY, it) }
        }
    }
}
