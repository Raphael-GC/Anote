package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AreaDao {
    @Query("SELECT * FROM areas ORDER BY sort_order")
    abstract fun observeAll(): Flow<List<Area>>

    @Query("SELECT * FROM areas ORDER BY sort_order")
    abstract suspend fun getAll(): List<Area>

    @Query("SELECT COUNT(*) FROM areas")
    abstract suspend fun count(): Int

    @Insert
    abstract suspend fun insertAll(areas: List<Area>)

    @Query("DELETE FROM areas")
    protected abstract suspend fun deleteAll()

    @Transaction
    open suspend fun replaceAll(areas: List<Area>) {
        deleteAll()
        insertAll(areas)
    }
}
