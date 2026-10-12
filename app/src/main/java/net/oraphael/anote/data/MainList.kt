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

enum class SortOrder { OLDEST, DUE_SOONEST, NEWEST }

private fun SortOrder.comparator(): Comparator<Entry> = when (this) {
    SortOrder.OLDEST -> compareBy<Entry> { it.createdAt }.thenBy { it.id }
    SortOrder.DUE_SOONEST -> compareBy<Entry, LocalDate?>(nullsLast()) { it.dueDate }
        .thenBy { it.createdAt }.thenBy { it.id }
    SortOrder.NEWEST -> compareByDescending<Entry> { it.createdAt }.thenByDescending { it.id }
}

data class MainList(
    val focuses: List<ListEntry>,
    val tasks: List<ListEntry>,
) {
    val isEmpty get() = focuses.isEmpty() && tasks.isEmpty()
}

// Focos do dia no topo, em qualquer ordenação; depois as demais tarefas pendentes.
// Por padrão, da mais antiga para a mais recente (`createdAt` nunca muda).
fun buildMainList(
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    focuses: List<Focus>,
    order: SortOrder = SortOrder.OLDEST,
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
        .sortedWith(order.comparator())
        .partition { it.id in focusIds }
    return MainList(
        focuses = focused.map(::item),
        tasks = others.map(::item),
    )
}

// Lista principal ao vivo: muda sempre que registros, áreas ou focos mudam.
class MainListSource(private val database: AnoteDatabase) {
    fun observe(today: LocalDate, order: SortOrder): Flow<MainList> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
        database.focusDao().observeForDate(today),
    ) { entries, links, areas, focuses -> buildMainList(entries, links, areas, focuses, order) }
}
