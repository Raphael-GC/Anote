package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import java.time.LocalDate
import java.time.ZoneId

data class CompletedDay(val date: LocalDate, val entries: List<ListEntry>)

// Concluídas agrupadas pelo dia da conclusão, da mais recente para a mais antiga.
fun groupCompleted(
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    zone: ZoneId,
): List<CompletedDay> {
    val areaById = areas.associateBy { it.id }
    val areasByEntry = links.groupBy({ it.entryId }, { areaById[it.areaId] })
    return entries
        .filter { it.completedAt != null }
        .sortedWith(compareByDescending<Entry> { it.completedAt }.thenByDescending { it.id })
        .map { ListEntry(it, areasByEntry[it.id].orEmpty().filterNotNull().sortedBy { a -> a.sortOrder }) }
        .groupBy { LocalDate.ofInstant(requireNotNull(it.entry.completedAt), zone) }
        .map { (date, items) -> CompletedDay(date, items) }
}

class CompletedSource(private val database: AnoteDatabase) {
    fun observe(zone: ZoneId = ZoneId.systemDefault()): Flow<List<CompletedDay>> = combine(
        database.entryDao().observeDone(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
    ) { entries, links, areas -> groupCompleted(entries, links, areas, zone) }
}
