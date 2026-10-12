package net.oraphael.anote.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.MainActivity
import net.oraphael.anote.R
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Horário fixo do aviso de prazo, pela manhã.
val DUE_NOTICE_TIME: LocalTime = LocalTime.of(9, 0)

data class DueCounts(val today: Int, val overdue: Int) {
    val isEmpty get() = today == 0 && overdue == 0
}

fun dueCounts(entries: List<Entry>, today: LocalDate): DueCounts {
    val pending = entries.filter { it.type == EntryType.TASK && it.status == EntryStatus.PENDING }
    return DueCounts(
        today = pending.count { it.dueDate == today },
        overdue = pending.count { it.dueDate != null && it.dueDate < today },
    )
}

// Aviso de prazo: opcional e desligado por padrão.
object DueNotice {
    const val CHANNEL_ID = "due"
    private const val WORK_NAME = "due-notice"
    private const val NOTIFICATION_ID = 9

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_due),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun update(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val now = LocalDateTime.now()
        val request = OneTimeWorkRequestBuilder<DueNoticeWorker>()
            .setInitialDelay(Duration.between(now, nextOccurrence(now, DUE_NOTICE_TIME)))
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    internal fun notify(context: Context, counts: DueCounts) {
        if (!ReviewReminders.canNotify(context)) return
        val resources = context.resources
        val text = listOfNotNull(
            counts.today.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.notification_due_today, it, it) },
            counts.overdue.takeIf { it > 0 }?.let { resources.getQuantityString(R.plurals.notification_due_overdue, it, it) },
        ).joinToString(" · ")
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context, NOTIFICATION_ID, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_due_title))
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    class DueNoticeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val app = applicationContext as AnoteApp
            val prefs = app.preferencesRepository.preferences.first()
            if (!prefs.dueNoticeEnabled) return Result.success()
            val counts = dueCounts(app.database.entryDao().observePending().first(), LocalDate.now())
            if (prefs.onboardingCompleted && !counts.isEmpty) notify(applicationContext, counts)
            update(applicationContext, enabled = true)
            return Result.success()
        }
    }
}
