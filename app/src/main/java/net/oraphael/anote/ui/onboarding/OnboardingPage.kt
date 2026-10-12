package net.oraphael.anote.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.oraphael.anote.ui.theme.Spacing

// Página do onboarding: título, conteúdo rolável e o botão de avançar fixo embaixo.
@Composable
fun OnboardingPage(
    title: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    primaryEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(Spacing.xl),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            content()
        }
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.l),
        ) {
            Text(primaryLabel)
        }
    }
}
