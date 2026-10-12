package net.oraphael.anote.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.data.MAX_AREAS_PER_ENTRY
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.ui.theme.Spacing

// Até 3 áreas: com 3 marcadas, as demais ficam desabilitadas.
@Composable
fun AreaPicker(areas: List<Area>, selected: Set<Long>, onChange: (Set<Long>) -> Unit) {
    val full = selected.size >= MAX_AREAS_PER_ENTRY
    Column {
        Text(stringResource(R.string.entry_areas_label), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            areas.forEach { area ->
                val isSelected = area.id in selected
                FilterChip(
                    selected = isSelected,
                    enabled = isSelected || !full,
                    onClick = { onChange(if (isSelected) selected - area.id else selected + area.id) },
                    label = { Text(area.name) },
                )
            }
        }
    }
}
