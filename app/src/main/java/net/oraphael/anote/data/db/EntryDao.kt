package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate

// Sem @Update genérico: cada alteração toca só as colunas necessárias,
// para que `created_at` nunca seja regravado.
@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getById(id: Long): Entry?

    @Query("UPDATE entries SET due_date = :dueDate WHERE id = :id")
    suspend fun updateDueDate(id: Long, dueDate: LocalDate)

    @Query("UPDATE entries SET manual_position = :position WHERE id = :id")
    suspend fun updateManualPosition(id: Long, position: Int?)
}
