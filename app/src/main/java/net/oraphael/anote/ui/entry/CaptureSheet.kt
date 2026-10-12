package net.oraphael.anote.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

// Captura rápida: texto e o tipo; para tarefa, também prazo (obrigatório) e áreas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(app: AnoteApp, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }
    val areas by app.areaRepository.areas.collectAsState(initial = emptyList())
    var text by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(EntryType.NOTE) }
    var due by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var areaIds by rememberSaveable { mutableStateOf(setOf<Long>()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val canSave = text.isNotBlank() && (type == EntryType.NOTE || due != null)

    fun save() {
        val content = text.trim()
        val dueDate = due
        scope.launch {
            when (type) {
                EntryType.NOTE -> app.entryRepository.createNote(content)
                EntryType.TASK -> app.entryRepository.createTask(content, requireNotNull(dueDate), areaIds.toList())
            }
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Text(stringResource(R.string.capture_title), style = MaterialTheme.typography.titleLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(EntryType.NOTE to R.string.capture_type_note, EntryType.TASK to R.string.capture_type_task)
                    .forEachIndexed { index, (option, label) ->
                        SegmentedButton(
                            selected = type == option,
                            onClick = { type = option },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                        ) { Text(stringResource(label)) }
                    }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.capture_placeholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focus),
            )
            if (type == EntryType.TASK) {
                DueDatePicker(selected = due, today = today, onSelect = { due = it })
                AreaPicker(areas = areas, selected = areaIds, onChange = { areaIds = it })
            }
            Button(onClick = ::save, enabled = canSave, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.capture_save))
            }
        }
    }
}
