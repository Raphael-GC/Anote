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

        // Tarefa sempre com prazo, anotação sempre sem prazo, na inserção e na alteração.
        private const val DUE_DATE_RULE = """
            WHEN (NEW.type = 'TASK' AND NEW.due_date IS NULL)
              OR (NEW.type = 'NOTE' AND NEW.due_date IS NOT NULL)
            BEGIN
                SELECT RAISE(ABORT, 'tasks need a due date; notes have none');
            END
        """
        const val DUE_DATE_INSERT_TRIGGER =
            "CREATE TRIGGER IF NOT EXISTS entries_due_date_insert BEFORE INSERT ON entries $DUE_DATE_RULE"
        const val DUE_DATE_UPDATE_TRIGGER =
            "CREATE TRIGGER IF NOT EXISTS entries_due_date_update BEFORE UPDATE OF type, due_date ON entries $DUE_DATE_RULE"

        val callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL(CREATED_AT_IMMUTABLE_TRIGGER)
                db.execSQL(DUE_DATE_INSERT_TRIGGER)
                db.execSQL(DUE_DATE_UPDATE_TRIGGER)
            }
        }

        fun create(context: Context): AnoteDatabase =
            Room.databaseBuilder(context, AnoteDatabase::class.java, NAME)
                .addCallback(callback)
                .build()
    }
}
