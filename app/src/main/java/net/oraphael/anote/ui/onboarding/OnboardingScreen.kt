package net.oraphael.anote.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.UserPreferences

// Etapas, na ordem em que aparecem.
enum class OnboardingStep { WELCOME, BACKUP, THEME, AREAS, DUE_DATE, REVIEWS }

@Composable
fun OnboardingScreen(app: AnoteApp, preferences: UserPreferences) {
    val scope = rememberCoroutineScope()
    val steps = OnboardingStep.entries
    var step by rememberSaveable { mutableStateOf(steps.first()) }
    val isLast = step == steps.last()

    fun next() {
        if (isLast) {
            scope.launch { app.preferencesRepository.completeOnboarding() }
        } else {
            step = steps[step.ordinal + 1]
        }
    }

    BackHandler(enabled = step != steps.first()) { step = steps[step.ordinal - 1] }

    val nextLabel = stringResource(if (isLast) R.string.onboarding_finish else R.string.onboarding_next)
    Surface(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                (slideInHorizontally { if (forward) it / 4 else -it / 4 } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (forward) -it / 4 else it / 4 } + fadeOut())
            },
            label = "onboarding",
        ) { current ->
            when (current) {
                OnboardingStep.WELCOME -> WelcomeStep(nextLabel, ::next)
                OnboardingStep.BACKUP -> BackupStep(app, preferences, nextLabel, ::next)
                OnboardingStep.THEME -> ThemeStep(app, preferences, nextLabel, ::next)
                OnboardingStep.AREAS -> AreasStep(app, nextLabel, ::next)
                OnboardingStep.DUE_DATE -> DueDateStep(nextLabel, ::next)
                OnboardingStep.REVIEWS -> ReviewTimesStep(app, preferences, nextLabel, ::next)
            }
        }
    }
}
