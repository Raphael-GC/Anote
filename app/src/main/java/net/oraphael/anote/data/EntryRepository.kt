package net.oraphael.anote.data

import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryAreaDao
import net.oraphael.anote.data.db.EntryDao
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.FocusDao
import java.time.Clock
import java.time.LocalDate

const val MAX_AREAS_PER_ENTRY = 3

// Adiar e reordenar alteram apenas prazo e posição; a idade (`createdAt`) fica intacta.
// Tarefas nascem com prazo obrigatório; anotações nunca têm prazo.
class EntryRepository(
    private val entryDao: EntryDao,
    private val entryAreaDao: EntryAreaDao,
    private val focusDao: FocusDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun createTask(text: String, dueDate: LocalDate, areaIds: List<Long> = emptyList()): Long =
        create(Entry(type = EntryType.TASK, text = text, createdAt = clock.instant(), dueDate = dueDate), areaIds)

    suspend fun createNote(text: String, areaIds: List<Long> = emptyList()): Long =
        create(Entry(type = EntryType.NOTE, text = text, createdAt = clock.instant(), dueDate = null), areaIds)

    private suspend fun create(entry: Entry, areaIds: List<Long>): Long {
        checkAreas(areaIds)
        val id = entryDao.insert(entry)
        if (areaIds.isNotEmpty()) entryAreaDao.replace(id, areaIds.distinct())
        return id
    }

    suspend fun setAreas(entryId: Long, areaIds: List<Long>) {
        checkAreas(areaIds)
        entryAreaDao.replace(entryId, areaIds.distinct())
    }

    private fun checkAreas(areaIds: List<Long>) =
        require(areaIds.distinct().size <= MAX_AREAS_PER_ENTRY) { "No máximo $MAX_AREAS_PER_ENTRY áreas" }

    suspend fun postpone(entryId: Long, newDueDate: LocalDate) {
        val entry = requireNotNull(entryDao.getById(entryId)) { "Registro inexistente" }
        require(entry.type == EntryType.TASK) { "Anotações não têm prazo" }
        entryDao.updateDueDate(entryId, newDueDate)
    }

    suspend fun convertNoteToTask(entryId: Long, dueDate: LocalDate) {
        check(entryDao.convertNoteToTask(entryId, dueDate) == 1) { "Só anotações viram tarefas" }
    }

    suspend fun setManualPosition(entryId: Long, position: Int?) =
        entryDao.updateManualPosition(entryId, position)

    // Descartada sai da lista e não conta como concluída; o registro fica no banco.
    suspend fun discard(entryId: Long) = entryDao.markDiscarded(entryId)

    // Concluir marca também o foco do dia, se a tarefa for um deles.
    suspend fun complete(entryId: Long) {
        val now = clock.instant()
        entryDao.markDone(entryId, now)
        focusDao.markCompleted(entryId, LocalDate.ofInstant(now, clock.zone))
    }

    // Desfaz uma conclusão: a tarefa volta a pendente e o foco do dia em que
    // foi concluída deixa de contar como cumprido.
    suspend fun uncomplete(entryId: Long) {
        val completedAt = entryDao.getById(entryId)?.completedAt ?: return
        entryDao.markPending(entryId)
        focusDao.markNotCompleted(entryId, LocalDate.ofInstant(completedAt, clock.zone))
    }
}
