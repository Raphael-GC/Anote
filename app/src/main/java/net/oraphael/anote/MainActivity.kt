package net.oraphael.anote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.AnoteTheme
import net.oraphael.anote.ui.ProvisionalScreen
import net.oraphael.anote.ui.onboarding.OnboardingScreen
import net.oraphael.anote.ui.splash.AnoteSplash

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AnoteApp
        setContent {
            val loaded by app.preferencesRepository.preferences.collectAsState(initial = null)
            val preferences = loaded ?: UserPreferences()
            AnoteTheme(theme = preferences.theme, darkMode = preferences.darkMode) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loaded == null -> Unit
                        !preferences.onboardingCompleted -> OnboardingScreen(app, preferences)
                        else -> ProvisionalScreen(app, preferences)
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
