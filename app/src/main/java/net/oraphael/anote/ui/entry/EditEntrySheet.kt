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

// Detalhe de um registro. Anotação: transformar em tarefa, escolhendo o prazo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntrySheet(app: AnoteApp, item: ListEntry, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }
    val entry = item.entry
    var due by rememberSaveable(entry.id) { mutableStateOf(entry.dueDate) }

    fun close() = scope.launch {
        sheetState.hide()
        onDismiss()
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
                text = stringResource(
                    if (entry.type == EntryType.NOTE) R.string.edit_note_title else R.string.edit_task_title
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(entry.text, style = MaterialTheme.typography.titleMedium)
            if (entry.type == EntryType.NOTE) {
                Text(stringResource(R.string.edit_convert_hint), style = MaterialTheme.typography.bodyMedium)
                DueDatePicker(selected = due, today = today, onSelect = { due = it })
                Button(
                    onClick = {
                        val dueDate = due ?: return@Button
                        scope.launch {
                            app.entryRepository.convertNoteToTask(entry.id, dueDate)
                            close()
                        }
                    },
                    enabled = due != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.edit_convert_action)) }
            }
        }
    }
}
