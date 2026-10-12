package net.oraphael.anote.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Review
import net.oraphael.anote.data.db.ReviewType
import net.oraphael.anote.data.prefs.PreferencesRepository
import java.time.Clock
import java.time.LocalDate

// Revisão diária: tarefas pendentes, da mais antiga para a mais recente (ou na
// ordem manual, se a revisão já foi reordenada hoje).
class ReviewRepository(
    private val database: AnoteDatabase,
    private val preferencesRepository: PreferencesRepository,
    private val focusRepository: FocusRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun pendingTasks(today: LocalDate): Flow<List<ListEntry>> = combine(
        database.entryDao().observePending(),
        database.entryAreaDao().observeAll(),
        database.areaDao().observeAll(),
        preferencesRepository.preferences,
    ) { entries, links, areas, prefs ->
        val manual = if (prefs.manualOrderDate == today) ManualScope.ALL else ManualScope.NONE
        buildMainList(entries, links, areas, emptyList(), SortOrder.OLDEST, manual).tasks
    }

    suspend fun focusIdsFor(date: LocalDate): Set<Long> =
        focusRepository.focusesFor(date).map { it.entryId }.toSet()

    // Conclui a revisão de `today`: grava a ordem, os focos de amanhã (na ordem
    // da lista) e o registro da revisão.
    suspend fun completeDaily(today: LocalDate, order: List<Long>, focusIds: Set<Long>) {
        saveManualOrder(order, today)
        focusRepository.chooseFocuses(today.plusDays(1), order.filter { it in focusIds })
        database.reviewDao().upsert(Review(ReviewType.DAILY, today, clock.instant()))
    }

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
