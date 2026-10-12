package net.oraphael.anote.data.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.flow.first
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.MainActivity
import net.oraphael.anote.R
import net.oraphael.anote.data.db.ReviewType
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

// Próximo instante (estritamente depois de `now`) com o horário `time`; com
// `day`, só nesse dia da semana.
fun nextOccurrence(now: LocalDateTime, time: LocalTime, day: DayOfWeek? = null): LocalDateTime {
    var candidate = now.toLocalDate().atTime(time)
    if (day != null) candidate = candidate.with(TemporalAdjusters.nextOrSame(day))
    if (!candidate.isAfter(now)) {
        candidate = if (day != null) candidate.plusWeeks(1) else candidate.plusDays(1)
    }
    return candidate
}

object ReviewReminders {
    const val CHANNEL_ID = "reviews"
    const val EXTRA_OPEN = "open"
    const val OPEN_DAILY_REVIEW = "daily_review"
    private const val KEY_TYPE = "type"
    private const val DAILY_NOTIFICATION_ID = 21

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_reviews),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Agenda o lembrete da revisão diária para a próxima ocorrência do horário.
    fun scheduleDaily(context: Context, time: LocalTime) {
        val delay = Duration.between(LocalDateTime.now(), nextOccurrence(LocalDateTime.now(), time))
        val request = OneTimeWorkRequestBuilder<ReviewReminderWorker>()
            .setInitialDelay(delay)
            .setInputData(workDataOf(KEY_TYPE to ReviewType.DAILY.name))
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("review-daily", ExistingWorkPolicy.REPLACE, request)
    }

    internal fun canNotify(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED && NotificationManagerCompat.from(context).areNotificationsEnabled()

    internal fun notifyDaily(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN, OPEN_DAILY_REVIEW)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_daily_title))
            .setContentText(context.getString(R.string.notification_daily_text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        if (canNotify(context)) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(DAILY_NOTIFICATION_ID, notification)
        }
    }

    class ReviewReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val app = applicationContext as AnoteApp
            val prefs = app.preferencesRepository.preferences.first()
            // Sem lembrete se a revisão de hoje já foi feita.
            val done = app.database.reviewDao().get(ReviewType.DAILY, LocalDate.now()) != null
            if (prefs.onboardingCompleted && !done) notifyDaily(applicationContext)
            scheduleDaily(applicationContext, prefs.dailyReviewTime)
            return Result.success()
        }
    }
}
