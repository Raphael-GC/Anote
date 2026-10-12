package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import net.oraphael.anote.data.prefs.PreferencesRepository
import java.time.LocalDate

data class ListEntry(val entry: Entry, val areas: List<Area>)

enum class SortOrder { OLDEST, DUE_SOONEST, NEWEST }

private fun SortOrder.comparator(): Comparator<Entry> = when (this) {
    SortOrder.OLDEST -> compareBy<Entry> { it.createdAt }.thenBy { it.id }
    SortOrder.DUE_SOONEST -> compareBy<Entry, LocalDate?>(nullsLast()) { it.dueDate }
        .thenBy { it.createdAt }.thenBy { it.id }
    SortOrder.NEWEST -> compareByDescending<Entry> { it.createdAt }.thenByDescending { it.id }
}

// Até onde vale a ordem manual feita na revisão: no próprio dia, para todas as
// tarefas; no dia seguinte, só para os focos; depois disso, para nenhuma.
enum class ManualScope { NONE, FOCUSES, ALL }

fun manualScope(manualOrderDate: LocalDate?, today: LocalDate): ManualScope = when (manualOrderDate) {
    today -> ManualScope.ALL
    today.minusDays(1) -> ManualScope.FOCUSES
    else -> ManualScope.NONE
}

data class MainList(
    val focuses: List<ListEntry>,
    val tasks: List<ListEntry>,
    val notes: List<ListEntry>,
) {
    val isEmpty get() = focuses.isEmpty() && tasks.isEmpty() && notes.isEmpty()
}

// Focos do dia no topo, em qualquer ordenação; depois as demais tarefas pendentes.
// Por padrão, da mais antiga para a mais recente (`createdAt` nunca muda), salvo
// a ordem manual ainda válida. Anotações ficam numa seção à parte; sem prazo,
// "prazo mais próximo" as mantém por idade.
fun buildMainList(
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    focuses: List<Focus>,
    order: SortOrder = SortOrder.OLDEST,
    manual: ManualScope = ManualScope.NONE,
): MainList {
    val areaById = areas.associateBy { it.id }
    val areasByEntry = links.groupBy({ it.entryId }, { areaById[it.areaId] })
    fun item(entry: Entry) = ListEntry(
        entry = entry,
        areas = areasByEntry[entry.id].orEmpty().filterNotNull().sortedBy { it.sortOrder },
    )

    // A ordem manual só substitui a ordem padrão; as outras ordenações são escolha explícita.
    val base = order.comparator()
    val byManual = compareBy<Entry, Int?>(nullsLast()) { it.manualPosition }.then(base)
    val useManual = order == SortOrder.OLDEST
    val focusComparator = if (useManual && manual != ManualScope.NONE) byManual else base
    val taskComparator = if (useManual && manual == ManualScope.ALL) byManual else base

    val focusIds = focuses.map { it.entryId }.toSet()
    val (focused, others) = entries
        .filter { it.type == EntryType.TASK }
        .partition { it.id in focusIds }
    return MainList(
        focuses = focused.sortedWith(focusComparator).map(::item),
        tasks = others.sortedWith(taskComparator).map(::item),
        notes = entries.filter { it.type == EntryType.NOTE }.sortedWith(base).map(::item),
    )
}

// Lista principal ao vivo: muda sempre que registros, áreas, focos ou a ordem manual mudam.
class MainListSource(
    private val database: AnoteDatabase,
    private val preferencesRepository: PreferencesRepository,
) {
    fun observe(today: LocalDate, order: SortOrder): Flow<MainList> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
        database.focusDao().observeForDate(today),
        preferencesRepository.preferences,
    ) { entries, links, areas, focuses, prefs ->
        buildMainList(entries, links, areas, focuses, order, manualScope(prefs.manualOrderDate, today))
    }
}
