package net.oraphael.anote.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.ageInDays
import net.oraphael.anote.ui.entry.DueCalendarDialog
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate

const val TRIAGE_SIZE = 5

// Triagem: para cada pendente mais antiga, manter, reagendar ou descartar.
@Composable
fun TriageSection(
    items: List<ListEntry>,
    today: LocalDate,
    onKeep: (Long) -> Unit,
    onReschedule: (Long, LocalDate) -> Unit,
    onDiscard: (Long) -> Unit,
) {
    var rescheduling by rememberSaveable { mutableStateOf<Long?>(null) }
    var discarding by rememberSaveable { mutableStateOf<Long?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(stringResource(R.string.triage_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(if (items.isEmpty()) R.string.triage_done else R.string.triage_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalColorTokens.current.onSurfaceVariant,
        )
        items.forEach { item ->
            TriageCard(
                item = item,
                today = today,
                onKeep = { onKeep(item.entry.id) },
                onReschedule = { rescheduling = item.entry.id },
                onDiscard = { discarding = item.entry.id },
            )
        }
    }

    rescheduling?.let { id ->
        val item = items.firstOrNull { it.entry.id == id }
        DueCalendarDialog(
            initial = item?.entry?.dueDate ?: today,
            today = today,
            onPick = {
                onReschedule(id, it)
                rescheduling = null
            },
            onDismiss = { rescheduling = null },
        )
    }

    discarding?.let { id ->
        AlertDialog(
            onDismissRequest = { discarding = null },
            title = { Text(stringResource(R.string.triage_discard_title)) },
            text = { Text(stringResource(R.string.triage_discard_body)) },
            confirmButton = {
                TextButton(onClick = {
                    onDiscard(id)
                    discarding = null
                }) { Text(stringResource(R.string.triage_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { discarding = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun TriageCard(
    item: ListEntry,
    today: LocalDate,
    onKeep: () -> Unit,
    onReschedule: () -> Unit,
    onDiscard: () -> Unit,
) {
    val colors = LocalColorTokens.current
    Surface(shape = MaterialTheme.shapes.medium, color = colors.surface, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(item.entry.text, style = MaterialTheme.typography.bodyLarge)
            val age = ageInDays(item.entry.createdAt, today)
            Text(
                text = if (age == 0) stringResource(R.string.age_today) else pluralStringResource(R.plurals.age_days, age, age),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                OutlinedButton(onClick = onKeep) { Text(stringResource(R.string.triage_keep)) }
                OutlinedButton(onClick = onReschedule) { Text(stringResource(R.string.triage_reschedule)) }
                TextButton(onClick = onDiscard) { Text(stringResource(R.string.triage_discard), color = colors.overdue) }
            }
        }
    }
}
