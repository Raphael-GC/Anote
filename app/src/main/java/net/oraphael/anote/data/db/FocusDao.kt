package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import java.time.LocalDate

@Dao
abstract class FocusDao {
    @Query("SELECT * FROM focuses WHERE date = :date")
    abstract suspend fun forDate(date: LocalDate): List<Focus>

    @Query("DELETE FROM focuses WHERE date = :date")
    protected abstract suspend fun clearDate(date: LocalDate)

    @Insert
    protected abstract suspend fun insertAll(focuses: List<Focus>)

    @Transaction
    open suspend fun replaceForDate(date: LocalDate, entryIds: List<Long>) {
        clearDate(date)
        insertAll(entryIds.map { Focus(date = date, entryId = it) })
    }

    @Query("UPDATE focuses SET completed = 1 WHERE entry_id = :entryId AND date = :date")
    abstract suspend fun markCompleted(entryId: Long, date: LocalDate)
}
