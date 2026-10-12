package net.oraphael.anote.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class EntryAreaDao {
    @Query("SELECT area_id FROM entry_areas WHERE entry_id = :entryId")
    abstract suspend fun areaIdsFor(entryId: Long): List<Long>

    @Query("DELETE FROM entry_areas WHERE entry_id = :entryId")
    protected abstract suspend fun clear(entryId: Long)

    @Insert
    protected abstract suspend fun insertAll(links: List<EntryArea>)

    @Transaction
    open suspend fun replace(entryId: Long, areaIds: List<Long>) {
        clear(entryId)
        insertAll(areaIds.map { EntryArea(entryId, it) })
    }
}
