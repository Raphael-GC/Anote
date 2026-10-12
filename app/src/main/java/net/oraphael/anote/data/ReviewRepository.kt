package net.oraphael.anote.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.prefs.PreferencesRepository
import java.time.LocalDate

// Revisão diária: tarefas pendentes, da mais antiga para a mais recente.
class ReviewRepository(
    private val database: AnoteDatabase,
    private val preferencesRepository: PreferencesRepository,
) {
    fun pendingTasks(): Flow<List<ListEntry>> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
    ) { entries, links, areas -> buildMainList(entries, links, areas, emptyList(), SortOrder.OLDEST).tasks }

    // Grava a ordem arrastada na revisão; vale até o fim de `today` (focos: até amanhã).
    suspend fun saveManualOrder(entryIds: List<Long>, today: LocalDate) {
        val entryDao = database.entryDao()
        database.withTransaction {
            entryDao.clearManualPositions()
            entryIds.forEachIndexed { position, id -> entryDao.updateManualPosition(id, position) }
        }
        preferencesRepository.setManualOrderDate(today)
    }
}
