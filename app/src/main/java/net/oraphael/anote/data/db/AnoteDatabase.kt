package net.oraphael.anote.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Entry::class, Area::class, EntryArea::class, Focus::class, Review::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AnoteDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        const val NAME = "anote.db"

        // Barreira no próprio banco: qualquer UPDATE que mude `created_at` é abortado.
        const val CREATED_AT_IMMUTABLE_TRIGGER = """
            CREATE TRIGGER IF NOT EXISTS entries_created_at_immutable
            BEFORE UPDATE OF created_at ON entries
            WHEN NEW.created_at IS NOT OLD.created_at
            BEGIN
                SELECT RAISE(ABORT, 'created_at is immutable');
            END
        """

        val callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATED_AT_IMMUTABLE_TRIGGER)
            }
        }

        fun create(context: Context): AnoteDatabase =
            Room.databaseBuilder(context, AnoteDatabase::class.java, NAME)
                .addCallback(callback)
                .build()
    }
}
