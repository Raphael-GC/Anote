package net.oraphael.anote.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.AreaNamesProblem
import net.oraphael.anote.data.MAX_AREA_NAME_LENGTH
import net.oraphael.anote.data.checkAreaNames
import net.oraphael.anote.ui.AppIcons
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

// Manter, editar ou criar áreas. Ao continuar, a lista é gravada.
@Composable
fun AreasStep(app: AnoteApp, nextLabel: String, onNext: () -> Unit) {
    val scope = rememberCoroutineScope()
    var names by rememberSaveable { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(Unit) {
        if (names == null) names = ArrayList(app.areaRepository.initialNames())
    }
    val current = names ?: return
    val problem = checkAreaNames(current)

    OnboardingPage(
        title = stringResource(R.string.onboarding_areas_title),
        primaryLabel = nextLabel,
        primaryEnabled = problem == null,
        onPrimary = {
            scope.launch {
                app.areaRepository.defineAreas(current)
                onNext()
            }
        },
    ) {
        Text(stringResource(R.string.onboarding_areas_body), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(R.string.onboarding_areas_fixed),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalColorTokens.current.overdue,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            current.forEachIndexed { index, name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { value ->
                            names = ArrayList(current).apply { set(index, value.take(MAX_AREA_NAME_LENGTH)) }
                        },
                        label = { Text(stringResource(R.string.onboarding_areas_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { names = ArrayList(current).apply { removeAt(index) } }) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(R.string.onboarding_areas_remove, name),
                        )
                    }
                }
            }
        }
        OutlinedButton(onClick = { names = ArrayList(current).apply { add("") } }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_areas_add))
        }
        problem?.let {
            Text(
                text = stringResource(
                    when (it) {
                        AreaNamesProblem.NONE_LEFT -> R.string.onboarding_areas_error_none
                        AreaNamesProblem.BLANK -> R.string.onboarding_areas_error_blank
                        AreaNamesProblem.DUPLICATE -> R.string.onboarding_areas_error_duplicate
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalColorTokens.current.overdue,
            )
        }
    }
}
