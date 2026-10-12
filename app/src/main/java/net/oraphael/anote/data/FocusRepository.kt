package net.oraphael.anote.data

import net.oraphael.anote.data.db.EntryDao
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import net.oraphael.anote.data.db.FocusDao
import java.time.LocalDate

const val MIN_FOCUSES = 3

// Focos: só tarefas pendentes, no mínimo 3 por dia (pode ser mais). Com menos de 3
// tarefas pendentes no total, o mínimo passa a ser todas elas.
class FocusRepository(
    private val entryDao: EntryDao,
    private val focusDao: FocusDao,
) {
    suspend fun focusesFor(date: LocalDate): List<Focus> = focusDao.forDate(date)

    suspend fun chooseFocuses(date: LocalDate, entryIds: List<Long>) {
        val ids = entryIds.distinct()
        ids.forEach { id ->
            val entry = requireNotNull(entryDao.getById(id)) { "Registro inexistente" }
            require(entry.type == EntryType.TASK && entry.status == EntryStatus.PENDING) {
                "Focos devem ser tarefas pendentes"
            }
        }
        val minimum = minOf(MIN_FOCUSES, entryDao.countPendingTasks())
        require(ids.size >= minimum) { "Escolha pelo menos $minimum focos" }
        focusDao.replaceForDate(date, ids)
    }
}
