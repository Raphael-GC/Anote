package net.oraphael.anote.ui.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.prefs.UserPreferences
import net.oraphael.anote.ui.isDark
import net.oraphael.anote.ui.theme.ThemePicker

// O tema escolhido já se aplica ao próprio onboarding.
@Composable
fun ThemeStep(app: AnoteApp, preferences: UserPreferences, nextLabel: String, onNext: () -> Unit) {
    val scope = rememberCoroutineScope()
    OnboardingPage(
        title = stringResource(R.string.onboarding_theme_title),
        primaryLabel = nextLabel,
        onPrimary = onNext,
    ) {
        Text(stringResource(R.string.onboarding_theme_body), style = MaterialTheme.typography.bodyLarge)
        ThemePicker(
            selected = preferences.theme,
            dark = preferences.darkMode.isDark(),
            onSelect = { scope.launch { app.preferencesRepository.setTheme(it) } },
        )
    }
}
