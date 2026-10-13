package net.oraphael.anote.data

import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.EntryStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class FocusRepositoryTest {
    private lateinit var db: AnoteDatabase
    private lateinit var clock: MutableClock
    private lateinit var entries: EntryRepository
    private lateinit var focuses: FocusRepository
    private val today = LocalDate.of(2026, 10, 12)

    @Before
    fun setUp() {
        db = inMemoryDatabase()
        clock = MutableClock()
        entries = EntryRepository(db.entryDao(), db.entryAreaDao(), db.focusDao(), clock)
        focuses = FocusRepository(db.entryDao(), db.focusDao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun createTasks(count: Int): List<Long> =
        List(count) { entries.createTask("Tarefa $it", today.plusDays(1)) }

    @Test
    fun requiresAtLeastThreeFocuses() = runTest {
        val tasks = createTasks(5)
        assertFailsWith<IllegalArgumentException> { focuses.chooseFocuses(today, tasks.take(2)) }
        focuses.chooseFocuses(today, tasks.take(3))
        assertEquals(3, focuses.focusesFor(today).size)
    }

    @Test
    fun allowsMoreThanThree() = runTest {
        val tasks = createTasks(5)
        focuses.chooseFocuses(today, tasks)
        assertEquals(5, focuses.focusesFor(today).size)
    }

    @Test
    fun repeatedTaskCountsOnce() = runTest {
        val tasks = createTasks(3)
        assertFailsWith<IllegalArgumentException> {
            focuses.chooseFocuses(today, listOf(tasks[0], tasks[0], tasks[1]))
        }
    }

    @Test
    fun withFewerThanThreePendingTasksAllOfThemAreEnough() = runTest {
        val tasks = createTasks(2)
        focuses.chooseFocuses(today, tasks)
        assertEquals(2, focuses.focusesFor(today).size)
    }

    @Test
    fun onlyPendingTasksCanBeFocuses() = runTest {
        val tasks = createTasks(4)
        val note = entries.createNote("Ideia")
        assertFailsWith<IllegalArgumentException> { focuses.chooseFocuses(today, tasks.take(3) + note) }

        entries.complete(tasks[3])
        assertFailsWith<IllegalArgumentException> { focuses.chooseFocuses(today, tasks) }
    }

    @Test
    fun choosingAgainReplacesTheDay() = runTest {
        val tasks = createTasks(6)
        focuses.chooseFocuses(today, tasks.take(3))
        focuses.chooseFocuses(today, tasks.drop(3))
        assertEquals(tasks.drop(3).toSet(), focuses.focusesFor(today).map { it.entryId }.toSet())
    }

    @Test
    fun completingAFocusedTaskMarksTheFocus() = runTest {
        val tasks = createTasks(3)
        focuses.chooseFocuses(today, tasks)
        entries.complete(tasks[0])

        val byEntry = focuses.focusesFor(today).associateBy { it.entryId }
        assertTrue(byEntry.getValue(tasks[0]).completed)
        assertFalse(byEntry.getValue(tasks[1]).completed)
    }

    @Test
    fun undoingACompletionRestoresTaskAndFocus() = runTest {
        val tasks = createTasks(3)
        focuses.chooseFocuses(today, tasks)
        entries.complete(tasks[0])
        // Desfazer depois da meia-noite ainda desmarca o foco do dia da conclusão.
        clock.now = clock.now.plusSeconds(13 * 3600)
        entries.uncomplete(tasks[0])

        val entry = requireNotNull(db.entryDao().getById(tasks[0]))
        assertEquals(EntryStatus.PENDING, entry.status)
        assertNull(entry.completedAt)
        assertFalse(focuses.focusesFor(today).single { it.entryId == tasks[0] }.completed)
    }

    @Test
    fun undoingLeavesPendingAndDiscardedAlone() = runTest {
        val tasks = createTasks(2)
        entries.discard(tasks[1])
        entries.uncomplete(tasks[0])
        entries.uncomplete(tasks[1])

        assertEquals(EntryStatus.PENDING, db.entryDao().getById(tasks[0])?.status)
        assertEquals(EntryStatus.DISCARDED, db.entryDao().getById(tasks[1])?.status)
    }

    @Test
    fun undoingADiscardBringsTheTaskBack() = runTest {
        val task = createTasks(1).single()
        entries.discard(task)
        entries.undoDiscard(task)
        assertEquals(EntryStatus.PENDING, db.entryDao().getById(task)?.status)
    }
}
