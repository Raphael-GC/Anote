package net.oraphael.anote.ui.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R

@Composable
fun DueDateStep(nextLabel: String, onNext: () -> Unit) {
    OnboardingPage(
        title = stringResource(R.string.onboarding_due_title),
        primaryLabel = nextLabel,
        onPrimary = onNext,
    ) {
        Text(stringResource(R.string.onboarding_due_body), style = MaterialTheme.typography.bodyLarge)
    }
}
