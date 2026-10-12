package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM areas ORDER BY sort_order")
    fun observeAll(): Flow<List<Area>>

    @Query("SELECT COUNT(*) FROM areas")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(areas: List<Area>)
}
