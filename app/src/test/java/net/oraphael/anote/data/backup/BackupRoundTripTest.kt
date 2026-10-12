package net.oraphael.anote.data.backup

import kotlinx.coroutines.test.runTest
import net.oraphael.anote.data.EntryRepository
import net.oraphael.anote.data.FocusRepository
import net.oraphael.anote.data.MutableClock
import net.oraphael.anote.data.assertFailsWith
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Review
import net.oraphael.anote.data.db.ReviewType
import net.oraphael.anote.data.inMemoryDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class BackupRoundTripTest {
    private lateinit var source: AnoteDatabase
    private lateinit var target: AnoteDatabase
    private val clock = MutableClock()
    private val today = LocalDate.of(2026, 10, 12)

    @Before
    fun setUp() {
        source = inMemoryDatabase()
        target = inMemoryDatabase()
    }

    @After
    fun tearDown() {
        source.close()
        target.close()
    }

    // Dados variados: áreas, tarefas com e sem áreas, anotação, conclusão,
    // posição manual, focos e revisões, com acentos no texto.
    private suspend fun populate(db: AnoteDatabase) {
        db.areaDao().insertAll(listOf(Area(name = "Casa", sortOrder = 0), Area(name = "Saúde", sortOrder = 1)))
        val entries = EntryRepository(db.entryDao(), db.entryAreaDao(), db.focusDao(), clock)
        val t1 = entries.createTask("Pagar a conta de luz", today.plusDays(1), listOf(1, 2))
        clock.now = clock.now.plusSeconds(60)
        val t2 = entries.createTask("Marcar dentista", today, listOf(2))
        clock.now = clock.now.plusSeconds(60)
        val t3 = entries.createTask("Revisar \"orçamento\"\ncom quebra de linha", today.plusDays(3))
        entries.createNote("Ideia: café às 7h ☕", listOf(1))
        entries.setManualPosition(t3, 0)
        FocusRepository(db.entryDao(), db.focusDao()).chooseFocuses(today, listOf(t1, t2, t3))
        entries.complete(t2)
        db.backupDao().insertReviews(
            listOf(Review(ReviewType.DAILY, today.minusDays(1), Instant.parse("2026-10-11T21:05:00Z")))
        )
    }

    private suspend fun contentOf(db: AnoteDatabase) =
        BackupRepository(db, clock).snapshot().copy(exportedAt = Instant.EPOCH)

    private suspend fun exportJson(db: AnoteDatabase) =
        BackupJson.encode(BackupRepository(db, clock).snapshot())

    @Test
    fun exportThenReplaceProducesTheSameData() = runTest {
        populate(source)
        BackupRepository(target, clock).import(BackupJson.decode(exportJson(source)), ImportMode.REPLACE)
        assertEquals(contentOf(source), contentOf(target))
    }

    @Test
    fun exportThenMergeIntoEmptyDatabaseProducesTheSameData() = runTest {
        populate(source)
        BackupRepository(target, clock).import(BackupJson.decode(exportJson(source)), ImportMode.MERGE)
        assertEquals(contentOf(source), contentOf(target))
    }

    @Test
    fun mergingTheSameFileTwiceDoesNotDuplicate() = runTest {
        populate(source)
        val data = BackupJson.decode(exportJson(source))
        val repository = BackupRepository(target, clock)
        repository.import(data, ImportMode.MERGE)
        repository.import(data, ImportMode.MERGE)
        assertEquals(contentOf(source), contentOf(target))
    }

    @Test
    fun replaceDiscardsCurrentData() = runTest {
        populate(source)
        populate(target)
        EntryRepository(target.entryDao(), target.entryAreaDao(), target.focusDao(), clock)
            .createNote("Só no destino")
        BackupRepository(target, clock).import(BackupJson.decode(exportJson(source)), ImportMode.REPLACE)
        assertEquals(contentOf(source), contentOf(target))
    }

    @Test
    fun invalidContentRollsBackTheWholeImport() = runTest {
        populate(target)
        val before = contentOf(target)
        // Tarefa sem prazo: o gatilho do banco recusa e nada deve mudar.
        val broken = BackupData(
            exportedAt = clock.now,
            areas = emptyList(),
            entries = listOf(
                Entry(id = 99, type = EntryType.TASK, text = "Sem prazo", createdAt = clock.now, dueDate = null)
            ),
            entryAreas = emptyList(),
            focuses = emptyList(),
            reviews = emptyList(),
        )
        val error = assertFailsWith<BackupFormatException> {
            BackupRepository(target, clock).import(broken, ImportMode.REPLACE)
        }
        assertEquals(BackupFormatException.Reason.CORRUPTED, error.reason)
        assertEquals(before, contentOf(target))
    }
}
