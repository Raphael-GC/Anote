package net.oraphael.anote.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.oraphael.anote.AnoteApp
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

// Detalhe de um registro: prazo (tarefa) e áreas podem ser alterados a qualquer
// momento. Numa anotação, escolher um prazo a transforma em tarefa.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntrySheet(app: AnoteApp, item: ListEntry, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }
    val allAreas by app.areaRepository.areas.collectAsState(initial = emptyList())
    val entry = item.entry
    val isNote = entry.type == EntryType.NOTE
    val originalAreas = remember(entry.id) { item.areas.map { it.id }.toSet() }
    var due by rememberSaveable(entry.id) { mutableStateOf(entry.dueDate) }
    var areaIds by rememberSaveable(entry.id) { mutableStateOf(originalAreas) }

    val dueChanged = due != entry.dueDate
    val areasChanged = areaIds != originalAreas
    val converting = isNote && due != null

    fun save() {
        val newDue = due
        scope.launch {
            if (areasChanged) app.entryRepository.setAreas(entry.id, areaIds.toList())
            when {
                converting && newDue != null -> app.entryRepository.convertNoteToTask(entry.id, newDue)
                !isNote && dueChanged && newDue != null -> app.entryRepository.postpone(entry.id, newDue)
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
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Text(
                text = stringResource(if (isNote) R.string.edit_note_title else R.string.edit_task_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(entry.text, style = MaterialTheme.typography.titleMedium)
            if (isNote) {
                Text(stringResource(R.string.edit_convert_hint), style = MaterialTheme.typography.bodyMedium)
            }
            DueDatePicker(selected = due, today = today, onSelect = { due = it })
            AreaPicker(areas = allAreas, selected = areaIds, onChange = { areaIds = it })
            Button(
                onClick = ::save,
                enabled = dueChanged || areasChanged,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (converting) R.string.edit_convert_action else R.string.edit_save))
            }
        }
    }
}
