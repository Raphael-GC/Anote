package net.oraphael.anote.ui.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.oraphael.anote.R
import net.oraphael.anote.data.backup.BackupProblem
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

// Aviso de que o último backup automático não pôde ser gravado.
@Composable
fun BackupWarning(problem: BackupProblem, onChooseFolder: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.overdue),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.l)) {
            Text(
                text = stringResource(R.string.backup_problem_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.overdue,
            )
            Text(
                text = stringResource(
                    when (problem) {
                        BackupProblem.PERMISSION_LOST -> R.string.backup_problem_permission
                        BackupProblem.FOLDER_UNAVAILABLE -> R.string.backup_problem_unavailable
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            TextButton(onClick = onChooseFolder) { Text(stringResource(R.string.backup_folder_choose)) }
        }
    }
}
