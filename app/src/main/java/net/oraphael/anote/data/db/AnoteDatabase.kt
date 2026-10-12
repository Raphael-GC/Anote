package net.oraphael.anote.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Entry::class, Area::class, EntryArea::class, Focus::class, Review::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AnoteDatabase : RoomDatabase() {
    companion object {
        const val NAME = "anote.db"
    }
}
