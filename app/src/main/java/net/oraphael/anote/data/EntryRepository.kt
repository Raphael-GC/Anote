package net.oraphael.anote.data

import net.oraphael.anote.data.db.EntryDao
import java.time.LocalDate

// Adiar e reordenar alteram apenas prazo e posição; a idade (`createdAt`) fica intacta.
class EntryRepository(private val entryDao: EntryDao) {
    suspend fun postpone(entryId: Long, newDueDate: LocalDate) =
        entryDao.updateDueDate(entryId, newDueDate)

    suspend fun setManualPosition(entryId: Long, position: Int?) =
        entryDao.updateManualPosition(entryId, position)
}
