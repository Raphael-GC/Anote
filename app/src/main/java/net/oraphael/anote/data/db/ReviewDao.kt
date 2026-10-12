package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface ReviewDao {
    // Refazer a revisão no mesmo dia só atualiza a hora de conclusão.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: Review)

    @Query("SELECT * FROM reviews WHERE type = :type AND date = :date")
    suspend fun get(type: ReviewType, date: LocalDate): Review?
}
