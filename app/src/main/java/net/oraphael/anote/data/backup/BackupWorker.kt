package net.oraphael.anote.data.backup

import android.content.Context
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import net.oraphael.anote.AnoteApp
import java.util.concurrent.TimeUnit

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as AnoteApp
        val folder = app.preferencesRepository.preferences.first().backupFolderUri?.toUri()
        app.autoBackup.run(folder)
        // Repetir não resolve permissão perdida; o próximo ciclo diário tenta de novo.
        return Result.success()
    }

    companion object {
        private const val DAILY = "auto-backup-daily"
        private const val NOW = "auto-backup-now"

        // Agenda o backup diário. Sem pasta escolhida, o worker não faz nada.
        fun scheduleDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        // Um backup imediato, por exemplo logo depois de escolher a pasta.
        fun runNow(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<BackupWorker>().build())
        }
    }
}
