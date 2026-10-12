package net.oraphael.anote.ui.entry

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val chipDate = DateTimeFormatter.ofPattern("EEE, d 'de' MMM", AppLocale)

// Prazo da tarefa: hoje, amanhã ou uma data do calendário (a partir de hoje).
@Composable
fun DueDatePicker(selected: LocalDate?, today: LocalDate, onSelect: (LocalDate) -> Unit) {
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    val tomorrow = today.plusDays(1)
    val custom = selected?.takeIf { it != today && it != tomorrow }

    Column {
        Text(stringResource(R.string.entry_due_label), style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            FilterChip(selected == today, { onSelect(today) }, { Text(stringResource(R.string.entry_due_today)) })
            FilterChip(selected == tomorrow, { onSelect(tomorrow) }, { Text(stringResource(R.string.entry_due_tomorrow)) })
            FilterChip(
                selected = custom != null,
                onClick = { showCalendar = true },
                label = {
                    Text(custom?.let { chipDate.format(it) } ?: stringResource(R.string.entry_due_pick))
                },
            )
        }
    }

    if (showCalendar) {
        DueCalendarDialog(
            initial = selected ?: today,
            today = today,
            onPick = {
                onSelect(it)
                showCalendar = false
            },
            onDismiss = { showCalendar = false },
        )
    }
}

// Calendário de prazo: só datas a partir de hoje.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueCalendarDialog(initial: LocalDate, today: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val todayMillis = today.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = maxOf(initial, today).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
            override fun isSelectableYear(year: Int) = year >= today.year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onPick(LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)) }
                },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}
