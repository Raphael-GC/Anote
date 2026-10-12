package net.oraphael.anote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.oraphael.anote.data.prefs.PreferencesRepository
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.data.prefs.preferencesDataStore
import net.oraphael.anote.ui.AnoteTheme
import net.oraphael.anote.ui.isDark
import net.oraphael.anote.ui.splash.AnoteSplash
import net.oraphael.anote.ui.theme.Spacing
import net.oraphael.anote.ui.theme.ThemePicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencesRepository = PreferencesRepository(applicationContext.preferencesDataStore)
        setContent {
            val preferences by preferencesRepository.preferences.collectAsState(initial = UserPreferences())
            val scope = rememberCoroutineScope()
            AnoteTheme(theme = preferences.theme, darkMode = preferences.darkMode) {
                var showSplash by rememberSaveable { mutableStateOf(true) }
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        // Tela provisória: hospeda o seletor de tema até o onboarding
                        // (Fase 4) e as Configurações (Fase 11) existirem.
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                                .padding(Spacing.l),
                            verticalArrangement = Arrangement.spacedBy(Spacing.l)
                        ) {
                            Text(
                                text = stringResource(R.string.theme_picker_title),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            ThemePicker(
                                selected = preferences.theme,
                                dark = preferences.darkMode.isDark(),
                                onSelect = { scope.launch { preferencesRepository.setTheme(it) } }
                            )
                        }
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
