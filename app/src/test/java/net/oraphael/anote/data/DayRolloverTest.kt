package net.oraphael.anote.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.ReviewType
import net.oraphael.anote.data.prefs.PreferencesRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

// Revisão de um dia e o que acontece nos dias seguintes.
@RunWith(RobolectricTestRunner::class)
class DayRolloverTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var db: AnoteDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var entries: EntryRepository
    private lateinit var review: ReviewRepository
    private lateinit var list: MainListSource
    private val clock = MutableClock()
    private val today = LocalDate.of(2026, 10, 12)

    @Before
    fun setUp() {
        db = inMemoryDatabase()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val prefs = PreferencesRepository(
            PreferenceDataStoreFactory.create(scope = scope) { tmp.newFile("prefs.preferences_pb") }
        )
        entries = EntryRepository(db.entryDao(), db.entryAreaDao(), db.focusDao(), clock)
        review = ReviewRepository(db, prefs, FocusRepository(db.entryDao(), db.focusDao()), clock)
        list = MainListSource(db, prefs)
    }

    @After
    fun tearDown() {
        db.close()
        scope.cancel()
    }

    // Cinco tarefas criadas em ordem (1 é a mais antiga).
    private suspend fun createTasks(): List<Long> = List(5) {
        clock.now = clock.now.plusSeconds(60)
        entries.createTask("Tarefa ${it + 1}", today.plusDays(5))
    }

    private suspend fun ids(date: LocalDate) = list.observe(date, SortOrder.OLDEST).first().let { l ->
        l.focuses.map { it.entry.id } to l.tasks.map { it.entry.id }
    }

    @Test
    fun manualOrderHoldsTodayThenOnlyForTomorrowsFocuses() = runTest {
        val (t1, t2, t3, t4, t5) = createTasks()
        // Na revisão de hoje: ordem 5, 3, 1, 4, 2; focos de amanhã: 4, 5, 2.
        review.completeDaily(today, listOf(t5, t3, t1, t4, t2), setOf(t4, t5, t2))

        assertEquals(emptyList<Long>() to listOf(t5, t3, t1, t4, t2), ids(today))
        assertEquals(listOf(t5, t4, t2) to listOf(t1, t3), ids(today.plusDays(1)))
        assertEquals(emptyList<Long>() to listOf(t1, t2, t3, t4, t5), ids(today.plusDays(2)))
        assertNotNull(db.reviewDao().get(ReviewType.DAILY, today))
    }

    @Test
    fun unfinishedTasksAndFocusesComeBackNextDay() = runTest {
        val (t1, t2, t3, t4, t5) = createTasks()
        review.completeDaily(today.minusDays(1), listOf(t1, t2, t3, t4, t5), setOf(t1, t2, t3))
        entries.complete(t2)

        // Hoje: focos 1 e 3 (o 2 foi concluído); nada some.
        assertEquals(listOf(t1, t3) to listOf(t4, t5), ids(today))
        // Amanhã, sem nova revisão: os focos não concluídos voltam a ser tarefas comuns.
        assertEquals(emptyList<Long>() to listOf(t1, t3, t4, t5), ids(today.plusDays(1)))
    }
}
