package net.oraphael.anote.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.oraphael.anote.R
import net.oraphael.anote.data.DueStatus
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.data.ageInDays
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.dueStatus
import net.oraphael.anote.ui.AppLocale
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val shortDate = DateTimeFormatter.ofPattern("d 'de' MMM", AppLocale)

@Composable
fun EntryRow(item: ListEntry, today: LocalDate, onComplete: (Long) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalColorTokens.current
    val isNote = item.entry.type == EntryType.NOTE
    val haptics = LocalHapticFeedback.current
    var completing by remember(item.entry.id) { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(if (completing) 0.5f else 1f, tween(COMPLETE_ANIMATION_MS), label = "alpha")
    LaunchedEffect(completing) {
        if (completing) {
            delay(COMPLETE_ANIMATION_MS.toLong())
            onComplete(item.entry.id)
        }
    }
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isNote) colors.surfaceVariant else colors.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isNote) {
                CompleteToggle(
                    checked = completing,
                    onCheck = {
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        completing = true
                    },
                    description = stringResource(R.string.complete_task, item.entry.text),
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
            EntryContent(item, today, Modifier.alpha(contentAlpha))
        }
    }
}

@Composable
private fun EntryContent(item: ListEntry, today: LocalDate, modifier: Modifier) {
    val colors = LocalColorTokens.current
    Column(
        modifier.padding(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(item.entry.text, style = MaterialTheme.typography.bodyLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            item.areas.forEach { area ->
                Text(
                    text = area.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSecondaryContainer,
                    modifier = Modifier
                        .background(colors.secondaryContainer, MaterialTheme.shapes.small)
                        .padding(horizontal = Spacing.s, vertical = 2.dp),
                )
            }
            item.entry.dueDate?.let { DueLabel(dueStatus(it, today)) }
            AgeLabel(ageInDays(item.entry.createdAt, today))
        }
    }
}

@Composable
private fun DueLabel(status: DueStatus) {
    val colors = LocalColorTokens.current
    val (text, color) = when (status) {
        is DueStatus.Overdue ->
            pluralStringResource(R.plurals.due_overdue, status.days, status.days) to colors.overdue
        DueStatus.Today -> stringResource(R.string.due_today) to colors.dueToday
        is DueStatus.Soon ->
            pluralStringResource(R.plurals.due_soon, status.days, status.days) to colors.dueSoon
        is DueStatus.Later ->
            stringResource(R.string.due_later, shortDate.format(status.date)) to colors.onSurfaceVariant
    }
    MetaText(text, color)
}

@Composable
private fun AgeLabel(days: Int) {
    MetaText(
        text = if (days == 0) stringResource(R.string.age_today) else pluralStringResource(R.plurals.age_days, days, days),
        color = LocalColorTokens.current.onSurfaceVariant,
    )
}

@Composable
private fun MetaText(text: String, color: Color) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = color)
}
