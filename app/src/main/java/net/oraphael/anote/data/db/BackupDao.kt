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

    @Insert
    suspend fun insertAreas(areas: List<Area>)

    @Insert
    suspend fun insertArea(area: Area): Long

    @Insert
    suspend fun insertEntries(entries: List<Entry>)

    @Insert
    suspend fun insertEntry(entry: Entry): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntryAreas(links: List<EntryArea>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFocuses(focuses: List<Focus>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviews(reviews: List<Review>)

    @Query("DELETE FROM entry_areas")
    suspend fun deleteEntryAreas()

    @Query("DELETE FROM focuses")
    suspend fun deleteFocuses()

    @Query("DELETE FROM reviews")
    suspend fun deleteReviews()

    @Query("DELETE FROM entries")
    suspend fun deleteEntries()

    @Query("DELETE FROM areas")
    suspend fun deleteAreas()
}
