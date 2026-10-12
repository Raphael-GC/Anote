package net.oraphael.anote.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase

// Revisão diária: tarefas pendentes, da mais antiga para a mais recente.
class ReviewRepository(private val database: AnoteDatabase) {
    fun pendingTasks(): Flow<List<ListEntry>> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
    ) { entries, links, areas -> buildMainList(entries, links, areas, emptyList(), SortOrder.OLDEST).tasks }
}
