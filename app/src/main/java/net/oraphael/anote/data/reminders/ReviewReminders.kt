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

// Lembretes das revisões: diária (todo dia) e de domingo (semanal).
object ReviewReminders {
    const val CHANNEL_ID = "reviews"
    const val EXTRA_OPEN = "open"
    const val OPEN_DAILY_REVIEW = "daily_review"
    const val OPEN_SUNDAY_REVIEW = "sunday_review"
    private const val KEY_TYPE = "type"

    private class Spec(
        val workName: String,
        val day: DayOfWeek?,
        val notificationId: Int,
        val title: Int,
        val text: Int,
        val open: String,
    )

    private fun spec(type: ReviewType) = when (type) {
        ReviewType.DAILY -> Spec(
            "review-daily", null, 21,
            R.string.notification_daily_title, R.string.notification_daily_text, OPEN_DAILY_REVIEW,
        )
        ReviewType.SUNDAY -> Spec(
            "review-sunday", DayOfWeek.SUNDAY, 19,
            R.string.notification_sunday_title, R.string.notification_sunday_text, OPEN_SUNDAY_REVIEW,
        )
    }

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_reviews),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Agenda o lembrete para a próxima ocorrência do horário.
    fun schedule(context: Context, type: ReviewType, time: LocalTime) {
        val spec = spec(type)
        val now = LocalDateTime.now()
        val request = OneTimeWorkRequestBuilder<ReviewReminderWorker>()
            .setInitialDelay(Duration.between(now, nextOccurrence(now, time, spec.day)))
            .setInputData(workDataOf(KEY_TYPE to type.name))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(spec.workName, ExistingWorkPolicy.REPLACE, request)
    }

    internal fun canNotify(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED && NotificationManagerCompat.from(context).areNotificationsEnabled()

    internal fun notify(context: Context, type: ReviewType) {
        if (!canNotify(context)) return
        val spec = spec(type)
        val intent = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN, spec.open)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context, spec.notificationId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(spec.title))
            .setContentText(context.getString(spec.text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission")
        NotificationManagerCompat.from(context).notify(spec.notificationId, notification)
    }

    class ReviewReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val type = inputData.getString(KEY_TYPE)?.let { runCatching { ReviewType.valueOf(it) }.getOrNull() }
                ?: return Result.success()
            val app = applicationContext as AnoteApp
            val prefs = app.preferencesRepository.preferences.first()
            // Sem lembrete se a revisão de hoje já foi feita.
            val done = app.database.reviewDao().get(type, LocalDate.now()) != null
            if (prefs.onboardingCompleted && prefs.remindersEnabled && !done) notify(applicationContext, type)
            val time = when (type) {
                ReviewType.DAILY -> prefs.dailyReviewTime
                ReviewType.SUNDAY -> prefs.sundayReviewTime
            }
            schedule(applicationContext, type, time)
            return Result.success()
        }
    }
}
