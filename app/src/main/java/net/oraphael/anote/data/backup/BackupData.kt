package net.oraphael.anote.data.backup

import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.Focus
import net.oraphael.anote.data.db.Review
import java.time.Instant

// Conteúdo de um arquivo de exportação ou backup.
data class BackupData(
    val exportedAt: Instant,
    val areas: List<Area>,
    val entries: List<Entry>,
    val entryAreas: List<EntryArea>,
    val focuses: List<Focus>,
    val reviews: List<Review>,
)
