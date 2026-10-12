package net.oraphael.anote.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.MainActivity
import net.oraphael.anote.R
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

val BACKUP_REMINDER_AFTER: Duration = Duration.ofDays(7)

// Lembrar quando o backup automático está desligado (sem pasta) ou quando o
// último backup tem mais de 7 dias.
fun needsBackupReminder(hasFolder: Boolean, lastBackupAt: Instant?, now: Instant): Boolean =
    !hasFolder || lastBackupAt == null || Duration.between(lastBackupAt, now) > BACKUP_REMINDER_AFTER

object BackupReminder {
    const val CHANNEL_ID = "backup"
    const val OPEN_BACKUP = "backup"
    private const val NOTIFICATION_ID = 7

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_backup),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<BackupReminderWorker>(7, TimeUnit.DAYS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("backup-reminder", ExistingPeriodicWorkPolicy.KEEP, request)
    }

    internal fun notify(context: Context, hasFolder: Boolean) {
        if (!ReviewReminders.canNotify(context)) return
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(ReviewReminders.EXTRA_OPEN, OPEN_BACKUP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context, NOTIFICATION_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_backup_title))
            .setContentText(
                context.getString(
                    if (hasFolder) R.string.notification_backup_stale else R.string.notification_backup_off
                )
            )
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    class BackupReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val app = applicationContext as AnoteApp
            val prefs = app.preferencesRepository.preferences.first()
            val status = app.backupStatusRepository.status.first()
            val hasFolder = prefs.backupFolderUri != null
            if (prefs.onboardingCompleted && prefs.remindersEnabled &&
                needsBackupReminder(hasFolder, status.lastBackupAt, Instant.now())
            ) {
                notify(applicationContext, hasFolder)
            }
            return Result.success()
        }
    }
}
