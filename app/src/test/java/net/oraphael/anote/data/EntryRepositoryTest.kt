package net.oraphael.anote.data

import android.database.sqlite.SQLiteException
import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class EntryRepositoryTest {
    private lateinit var db: AnoteDatabase
    private lateinit var clock: MutableClock
    private lateinit var repository: EntryRepository
    private val due = LocalDate.of(2026, 10, 15)

    @Before
    fun setUp() {
        db = inMemoryDatabase()
        clock = MutableClock()
        repository = EntryRepository(db.entryDao(), db.entryAreaDao(), db.focusDao(), clock)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun createAreas(count: Int): List<Long> {
        db.areaDao().insertAll(List(count) { Area(name = "Área $it", sortOrder = it) })
        return (1L..count.toLong()).toList()
    }

    // Prazo obrigatório em tarefas

    @Test
    fun taskKeepsItsDueDateAndNoteHasNone() = runTest {
        val task = db.entryDao().getById(repository.createTask("Pagar conta", due))!!
        val note = db.entryDao().getById(repository.createNote("Ideia"))!!
        assertEquals(due, task.dueDate)
        assertNull(note.dueDate)
    }

    @Test
    fun databaseRejectsTaskWithoutDueDate() = runTest {
        val entry = Entry(type = EntryType.TASK, text = "x", createdAt = clock.now, dueDate = null)
        assertFailsWith<SQLiteException> { db.entryDao().insert(entry) }
    }

    @Test
    fun databaseRejectsNoteWithDueDate() = runTest {
        val entry = Entry(type = EntryType.NOTE, text = "x", createdAt = clock.now, dueDate = due)
        assertFailsWith<SQLiteException> { db.entryDao().insert(entry) }
    }

    @Test
    fun noteCannotBePostponed() = runTest {
        val id = repository.createNote("Ideia")
        assertFailsWith<IllegalArgumentException> { repository.postpone(id, due) }
    }

    @Test
    fun noteBecomesTaskKeepingItsAge() = runTest {
        val created = clock.now
        val id = repository.createNote("Ideia")
        clock.now = Instant.parse("2026-10-14T09:00:00Z")
        repository.convertNoteToTask(id, due)

        val entry = db.entryDao().getById(id)!!
        assertEquals(EntryType.TASK, entry.type)
        assertEquals(due, entry.dueDate)
        assertEquals(created, entry.createdAt)
    }

    @Test
    fun taskCannotBeConvertedAgain() = runTest {
        val id = repository.createTask("Pagar conta", due)
        assertFailsWith<IllegalStateException> { repository.convertNoteToTask(id, due) }
    }

    @Test
    fun discardedTaskLeavesPendingWithoutCountingAsDone() = runTest {
        val id = repository.createTask("Pagar conta", due)
        repository.discard(id)
        val entry = db.entryDao().getById(id)!!
        assertEquals(EntryStatus.DISCARDED, entry.status)
        assertNull(entry.completedAt)
        assertEquals(0, db.entryDao().countPendingTasks())
    }

    // Imutabilidade de createdAt

    @Test
    fun postponeAndReorderKeepCreatedAt() = runTest {
        val created = clock.now
        val id = repository.createTask("Pagar conta", due)
        clock.now = Instant.parse("2026-10-14T09:00:00Z")
        repository.postpone(id, due.plusDays(3))
        repository.setManualPosition(id, 0)

        val entry = db.entryDao().getById(id)!!
        assertEquals(created, entry.createdAt)
        assertEquals(due.plusDays(3), entry.dueDate)
        assertEquals(0, entry.manualPosition)
    }

    @Test
    fun databaseRejectsChangingCreatedAt() = runTest {
        val id = repository.createTask("Pagar conta", due)
        assertThrows(SQLiteException::class.java) {
            db.openHelper.writableDatabase.execSQL("UPDATE entries SET created_at = 0 WHERE id = $id")
        }
        assertEquals(clock.now, db.entryDao().getById(id)!!.createdAt)
    }

    // Limite de 3 áreas

    @Test
    fun acceptsUpToThreeAreas() = runTest {
        val areas = createAreas(3)
        val id = repository.createTask("Pagar conta", due, areas)
        assertEquals(areas.toSet(), db.entryAreaDao().areaIdsFor(id).toSet())
    }

    @Test
    fun rejectsFourAreasOnCreateAndOnEdit() = runTest {
        val areas = createAreas(4)
        assertFailsWith<IllegalArgumentException> { repository.createTask("Pagar conta", due, areas) }
        val id = repository.createNote("Ideia", areas.take(2))
        assertFailsWith<IllegalArgumentException> { repository.setAreas(id, areas) }
        assertEquals(2, db.entryAreaDao().areaIdsFor(id).size)
    }

    @Test
    fun repeatedAreaCountsOnce() = runTest {
        val areas = createAreas(3)
        val id = repository.createTask("Pagar conta", due, areas + areas.first())
        assertEquals(3, db.entryAreaDao().areaIdsFor(id).size)
    }

    @Test
    fun databaseRejectsFourthArea() = runTest {
        val areas = createAreas(4)
        val id = repository.createTask("Pagar conta", due, areas.take(3))
        assertThrows(SQLiteException::class.java) {
            db.openHelper.writableDatabase.execSQL(
                "INSERT INTO entry_areas (entry_id, area_id) VALUES ($id, ${areas[3]})"
            )
        }
    }
}
