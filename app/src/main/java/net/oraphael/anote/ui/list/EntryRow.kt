package net.oraphael.anote.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.oraphael.anote.data.ListEntry
import net.oraphael.anote.ui.theme.LocalColorTokens
import net.oraphael.anote.ui.theme.Spacing

@Composable
fun EntryRow(item: ListEntry, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = LocalColorTokens.current.surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
    ) {
        Column(Modifier.padding(Spacing.m)) {
            Text(item.entry.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
