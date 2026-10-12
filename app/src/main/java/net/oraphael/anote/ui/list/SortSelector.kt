package net.oraphael.anote.ui.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.oraphael.anote.R
import net.oraphael.anote.data.SortOrder
import net.oraphael.anote.ui.theme.Spacing

@Composable
fun SortSelector(selected: SortOrder, onChange: (SortOrder) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        SortOrder.entries.forEach { order ->
            FilterChip(
                selected = order == selected,
                onClick = { onChange(order) },
                label = {
                    Text(
                        stringResource(
                            when (order) {
                                SortOrder.OLDEST -> R.string.sort_oldest
                                SortOrder.DUE_SOONEST -> R.string.sort_due_soonest
                                SortOrder.NEWEST -> R.string.sort_newest
                            }
                        )
                    )
                },
            )
        }
    }
}
