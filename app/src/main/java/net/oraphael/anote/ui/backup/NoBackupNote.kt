package net.oraphael.anote.ui.backup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

// Lembra, com destaque discreto, a escolha "seguir sem backup" do onboarding.
@Composable
fun NoBackupNote(modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.backup_no_backup_note),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.l),
        )
    }
}
