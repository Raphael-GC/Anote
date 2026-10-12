package net.oraphael.anote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.data.reminders.ReviewReminders
import net.oraphael.anote.ui.AnoteTheme
import net.oraphael.anote.ui.ProvisionalScreen
import net.oraphael.anote.ui.entry.CaptureSheet
import net.oraphael.anote.ui.list.MainListScreen
import net.oraphael.anote.ui.onboarding.OnboardingScreen
import net.oraphael.anote.ui.review.DailyReviewScreen
import net.oraphael.anote.ui.review.SundayReviewScreen
import net.oraphael.anote.ui.splash.AnoteSplash

private enum class Screen { HOME, SETTINGS, DAILY_REVIEW, SUNDAY_REVIEW }

class MainActivity : ComponentActivity() {
    // Pedido de abrir uma tela, vindo de uma notificação.
    private val openRequest = mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openRequest.value = intent.getStringExtra(ReviewReminders.EXTRA_OPEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AnoteApp
        if (savedInstanceState == null) openRequest.value = intent.getStringExtra(ReviewReminders.EXTRA_OPEN)
        setContent {
            val loaded by app.preferencesRepository.preferences.collectAsState(initial = null)
            val preferences = loaded ?: UserPreferences()
            AnoteTheme(theme = preferences.theme, darkMode = preferences.darkMode) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var showCapture by rememberSaveable { mutableStateOf(false) }
                val openCapture = { showCapture = true }
                val goHome = { screen = Screen.HOME }
                val request by openRequest
                LaunchedEffect(request) {
                    if (request == ReviewReminders.OPEN_DAILY_REVIEW) screen = Screen.DAILY_REVIEW
                    openRequest.value = null
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loaded == null -> Unit
                        !preferences.onboardingCompleted -> OnboardingScreen(app, preferences)
                        else -> {
                            BackHandler(enabled = screen != Screen.HOME, onBack = goHome)
                            when (screen) {
                                Screen.HOME -> MainListScreen(
                                    app,
                                    onOpenSettings = { screen = Screen.SETTINGS },
                                    onOpenReview = { screen = Screen.DAILY_REVIEW },
                                    onOpenWeek = { screen = Screen.SUNDAY_REVIEW },
                                    onCapture = openCapture,
                                )
                                Screen.SETTINGS -> ProvisionalScreen(app, preferences, onCapture = openCapture)
                                Screen.DAILY_REVIEW -> DailyReviewScreen(app, onClose = goHome)
                                Screen.SUNDAY_REVIEW -> SundayReviewScreen(app, preferences, onClose = goHome)
                            }
                        }
                    }
                    if (showCapture && preferences.onboardingCompleted) {
                        CaptureSheet(app, onDismiss = { showCapture = false })
                    }
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = EnterTransition.None,
                        exit = fadeOut()
                    ) {
                        AnoteSplash(onFinished = { showSplash = false })
                    }
                }
            }
        }
    }
}
