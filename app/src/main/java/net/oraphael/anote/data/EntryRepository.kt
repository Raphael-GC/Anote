package net.oraphael.anote.data

import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryDao
import net.oraphael.anote.data.db.EntryType
import java.time.Clock
import java.time.LocalDate

// Adiar e reordenar alteram apenas prazo e posição; a idade (`createdAt`) fica intacta.
// Tarefas nascem com prazo obrigatório; anotações nunca têm prazo.
class EntryRepository(
    private val entryDao: EntryDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun createTask(text: String, dueDate: LocalDate): Long =
        entryDao.insert(
            Entry(type = EntryType.TASK, text = text, createdAt = clock.instant(), dueDate = dueDate)
        )

    suspend fun createNote(text: String): Long =
        entryDao.insert(
            Entry(type = EntryType.NOTE, text = text, createdAt = clock.instant(), dueDate = null)
        )

    suspend fun postpone(entryId: Long, newDueDate: LocalDate) {
        val entry = requireNotNull(entryDao.getById(entryId)) { "Registro inexistente" }
        require(entry.type == EntryType.TASK) { "Anotações não têm prazo" }
        entryDao.updateDueDate(entryId, newDueDate)
    }

    suspend fun setManualPosition(entryId: Long, position: Int?) =
        entryDao.updateManualPosition(entryId, position)
}
