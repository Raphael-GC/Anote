package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import java.time.LocalDate

data class ListEntry(val entry: Entry, val areas: List<Area>)

data class MainList(
    val focuses: List<ListEntry>,
    val tasks: List<ListEntry>,
) {
    val isEmpty get() = focuses.isEmpty() && tasks.isEmpty()
}

// Focos do dia no topo; depois as demais tarefas pendentes, da mais antiga para a
// mais recente. `createdAt` nunca muda, então a idade é estável.
fun buildMainList(
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    focuses: List<Focus>,
): MainList {
    val areaById = areas.associateBy { it.id }
    val areasByEntry = links.groupBy({ it.entryId }, { areaById[it.areaId] })
    fun item(entry: Entry) = ListEntry(
        entry = entry,
        areas = areasByEntry[entry.id].orEmpty().filterNotNull().sortedBy { it.sortOrder },
    )

    val focusIds = focuses.map { it.entryId }.toSet()
    val (focused, others) = entries
        .filter { it.type == EntryType.TASK }
        .sortedWith(compareBy<Entry> { it.createdAt }.thenBy { it.id })
        .partition { it.id in focusIds }
    return MainList(
        focuses = focused.map(::item),
        tasks = others.map(::item),
    )
}

// Lista principal ao vivo: muda sempre que registros, áreas ou focos mudam.
class MainListSource(private val database: AnoteDatabase) {
    fun observe(today: LocalDate): Flow<MainList> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
        database.focusDao().observeForDate(today),
        ::buildMainList,
    )
}
