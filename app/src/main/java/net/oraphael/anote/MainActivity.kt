package net.oraphael.anote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.graphics.Color
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.data.reminders.BackupReminder
import net.oraphael.anote.data.reminders.ReviewReminders
import net.oraphael.anote.ui.AnoteTheme
import net.oraphael.anote.ui.isDark
import net.oraphael.anote.ui.dashboard.DashboardScreen
import net.oraphael.anote.ui.entry.CaptureSheet
import net.oraphael.anote.ui.list.MainListScreen
import net.oraphael.anote.ui.onboarding.OnboardingScreen
import net.oraphael.anote.ui.review.DailyReviewScreen
import net.oraphael.anote.ui.review.SundayReviewScreen
import net.oraphael.anote.ui.settings.SettingsScreen
import net.oraphael.anote.ui.splash.AnoteSplash
import net.oraphael.anote.widget.OPEN_CAPTURE

private enum class Screen { HOME, SETTINGS, DAILY_REVIEW, SUNDAY_REVIEW, DASHBOARD }

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
            // Ícones das barras do sistema seguem o modo escolhido no app, não o do sistema.
            val dark = preferences.darkMode.isDark()
            DisposableEffect(dark) {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            AnoteTheme(theme = preferences.theme, darkMode = preferences.darkMode) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var showCapture by rememberSaveable { mutableStateOf(false) }
                val openCapture = { showCapture = true }
                val goHome = { screen = Screen.HOME }
                val request by openRequest
                LaunchedEffect(request) {
                    when (request) {
                        ReviewReminders.OPEN_DAILY_REVIEW -> screen = Screen.DAILY_REVIEW
                        ReviewReminders.OPEN_SUNDAY_REVIEW -> screen = Screen.SUNDAY_REVIEW
                        OPEN_CAPTURE -> showCapture = true
                        BackupReminder.OPEN_BACKUP -> screen = Screen.SETTINGS
                    }
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
                                    onOpenDashboard = { screen = Screen.DASHBOARD },
                                    onCapture = openCapture,
                                )
                                Screen.SETTINGS -> SettingsScreen(app, preferences, onClose = goHome, onCapture = openCapture)
                                Screen.DAILY_REVIEW -> DailyReviewScreen(app, onClose = goHome)
                                Screen.SUNDAY_REVIEW -> SundayReviewScreen(app, preferences, onClose = goHome)
                                Screen.DASHBOARD -> DashboardScreen(app, onClose = goHome)
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
