package net.oraphael.anote.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.ui.splash.AnoteLogo
import net.oraphael.anote.ui.theme.Spacing

@Composable
fun WelcomeStep(nextLabel: String, onNext: () -> Unit) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_welcome_title),
        primaryLabel = nextLabel,
        onPrimary = onNext,
    ) {
        AnoteLogo(
            Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.l)
        )
        Text(stringResource(R.string.onboarding_welcome_body), style = MaterialTheme.typography.bodyLarge)
    }
}
