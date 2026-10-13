package net.oraphael.anote.ui.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.TimePickerDialog
import net.oraphael.anote.ui.formatTime
import java.time.LocalTime

private enum class Review { DAILY, SUNDAY }

@Composable
fun ReviewTimesStep(app: AnoteApp, preferences: UserPreferences, nextLabel: String, onNext: () -> Unit) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_reviews_title),
        primaryLabel = nextLabel,
        onPrimary = onNext,
    ) {
        Text(stringResource(R.string.onboarding_reviews_body), style = MaterialTheme.typography.bodyLarge)
        ReviewTimesEditor(app, preferences)
    }
}

// Horários das revisões diária e de domingo; usado no onboarding e nas Configurações.
@Composable
fun ReviewTimesEditor(app: AnoteApp, preferences: UserPreferences) {
    val scope = rememberCoroutineScope()
    var editing by rememberSaveable { mutableStateOf<Review?>(null) }
    val dailyLabel = stringResource(R.string.review_daily)
    val sundayLabel = stringResource(R.string.review_sunday)

    ReviewTimeRow(dailyLabel, preferences.dailyReviewTime) { editing = Review.DAILY }
    ReviewTimeRow(sundayLabel, preferences.sundayReviewTime) { editing = Review.SUNDAY }

    when (editing) {
        Review.DAILY -> TimePickerDialog(
            title = dailyLabel,
            initial = preferences.dailyReviewTime,
            onConfirm = {
                editing = null
                scope.launch { app.preferencesRepository.setDailyReviewTime(it) }
            },
            onDismiss = { editing = null },
        )
        Review.SUNDAY -> TimePickerDialog(
            title = sundayLabel,
            initial = preferences.sundayReviewTime,
            onConfirm = {
                editing = null
                scope.launch { app.preferencesRepository.setSundayReviewTime(it) }
            },
            onDismiss = { editing = null },
        )
        null -> Unit
    }
}

@Composable
private fun ReviewTimeRow(label: String, time: LocalTime, onChange: () -> Unit) {
    val changeDescription = stringResource(R.string.review_time_change, label)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onChange, modifier = Modifier.semantics { contentDescription = changeDescription }) {
            Text(formatTime(time), style = MaterialTheme.typography.titleLarge)
        }
    }
}
