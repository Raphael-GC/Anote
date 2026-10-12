package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Leitura e escrita em bloco para exportar, importar e fazer backup.
@Dao
interface BackupDao {
    @Query("SELECT * FROM areas ORDER BY sort_order")
    suspend fun allAreas(): List<Area>

    @Query("SELECT * FROM entries ORDER BY id")
    suspend fun allEntries(): List<Entry>

    @Query("SELECT * FROM entry_areas")
    suspend fun allEntryAreas(): List<EntryArea>

    @Query("SELECT * FROM focuses ORDER BY date")
    suspend fun allFocuses(): List<Focus>

    @Query("SELECT * FROM reviews ORDER BY date")
    suspend fun allReviews(): List<Review>
}
