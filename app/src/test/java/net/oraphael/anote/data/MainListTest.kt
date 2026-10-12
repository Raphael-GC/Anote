package net.oraphael.anote.data

import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class MainListTest {
    private val today = LocalDate.of(2026, 10, 12)

    // Criada `daysAgo` dias atrás, com prazo em `dueIn` dias.
    private fun task(id: Long, daysAgo: Long, dueIn: Long) = Entry(
        id = id,
        type = EntryType.TASK,
        text = "Tarefa $id",
        createdAt = today.minusDays(daysAgo).atStartOfDay().toInstant(ZoneOffset.UTC),
        dueDate = today.plusDays(dueIn),
    )

    private fun note(id: Long, daysAgo: Long) = Entry(
        id = id,
        type = EntryType.NOTE,
        text = "Anotação $id",
        createdAt = today.minusDays(daysAgo).atStartOfDay().toInstant(ZoneOffset.UTC),
        dueDate = null,
    )

    private fun focus(id: Long) = Focus(date = today, entryId = id)

    private val entries = listOf(
        task(1, daysAgo = 2, dueIn = 10),
        task(2, daysAgo = 9, dueIn = 3),
        task(3, daysAgo = 5, dueIn = -4),
        task(4, daysAgo = 1, dueIn = 0),
        task(5, daysAgo = 7, dueIn = 1),
        note(6, daysAgo = 3),
        note(7, daysAgo = 8),
    )
    private val focuses = listOf(focus(1), focus(4))

    private fun build(order: SortOrder) = buildMainList(entries.shuffled(), emptyList(), emptyList(), focuses, order)

    private fun List<ListEntry>.ids() = map { it.entry.id }

    @Test
    fun focusesOnTopThenOldestFirst() {
        val list = build(SortOrder.OLDEST)
        assertEquals(listOf(1L, 4L), list.focuses.ids())
        assertEquals(listOf(2L, 5L, 3L), list.tasks.ids())
    }

    @Test
    fun dueSoonestKeepsFocusesOnTop() {
        val list = build(SortOrder.DUE_SOONEST)
        assertEquals(listOf(4L, 1L), list.focuses.ids())
        assertEquals(listOf(3L, 5L, 2L), list.tasks.ids())
    }

    @Test
    fun newestKeepsFocusesOnTop() {
        val list = build(SortOrder.NEWEST)
        assertEquals(listOf(4L, 1L), list.focuses.ids())
        assertEquals(listOf(3L, 5L, 2L), list.tasks.ids())
    }

    @Test
    fun notesAreSeparateAndOrderedByAge() {
        assertEquals(listOf(7L, 6L), build(SortOrder.OLDEST).notes.ids())
        assertEquals(listOf(7L, 6L), build(SortOrder.DUE_SOONEST).notes.ids())
        assertEquals(listOf(6L, 7L), build(SortOrder.NEWEST).notes.ids())
    }

    @Test
    fun overdueTaskKeepsItsAgePosition() {
        // A tarefa 3 venceu há 4 dias, mas continua entre a 5 (mais antiga) e a 1 (mais nova).
        assertEquals(listOf(2L, 5L, 3L), build(SortOrder.OLDEST).tasks.ids())
    }

    @Test
    fun sameCreationTimeFallsBackToId() {
        val created = Instant.parse("2026-10-01T10:00:00Z")
        val twins = listOf(
            Entry(id = 9, type = EntryType.TASK, text = "b", createdAt = created, dueDate = today),
            Entry(id = 8, type = EntryType.TASK, text = "a", createdAt = created, dueDate = today),
        )
        assertEquals(listOf(8L, 9L), buildMainList(twins, emptyList(), emptyList(), emptyList()).tasks.ids())
    }

    @Test
    fun attachesAreasInTheirOrder() {
        val areas = listOf(Area(1, "Casa", 1), Area(2, "Trabalho", 0))
        val links = listOf(EntryArea(1, 1), EntryArea(1, 2))
        val list = buildMainList(listOf(task(1, 1, 1)), links, areas, emptyList())
        assertEquals(listOf("Trabalho", "Casa"), list.tasks.single().areas.map { it.name })
    }

    @Test
    fun emptyWhenNothingPending() {
        assertTrue(buildMainList(emptyList(), emptyList(), emptyList(), focuses).isEmpty)
    }

    @Test
    fun dueStatusWindows() {
        assertEquals(DueStatus.Overdue(4), dueStatus(today.minusDays(4), today))
        assertEquals(DueStatus.Today, dueStatus(today, today))
        assertEquals(DueStatus.Soon(1), dueStatus(today.plusDays(1), today))
        assertEquals(DueStatus.Soon(SOON_DAYS), dueStatus(today.plusDays(SOON_DAYS.toLong()), today))
        assertEquals(DueStatus.Later(today.plusDays(8)), dueStatus(today.plusDays(8), today))
    }

    @Test
    fun ageCountsCalendarDays() {
        val created = Instant.parse("2026-10-09T23:30:00Z")
        assertEquals(3, ageInDays(created, today, ZoneOffset.UTC))
        assertEquals(0, ageInDays(Instant.parse("2026-10-12T08:00:00Z"), today, ZoneOffset.UTC))
    }

    @Test
    fun manualScopeByDay() {
        assertEquals(ManualScope.ALL, manualScope(today, today))
        assertEquals(ManualScope.FOCUSES, manualScope(today.minusDays(1), today))
        assertEquals(ManualScope.NONE, manualScope(today.minusDays(2), today))
        assertEquals(ManualScope.NONE, manualScope(null, today))
    }

    @Test
    fun manualOrderOnlyReplacesTheDefaultOrder() {
        val manual = entries.map { if (it.id == 3L) it.copy(manualPosition = 0) else it }
        val all = buildMainList(manual, emptyList(), emptyList(), focuses, SortOrder.OLDEST, ManualScope.ALL)
        assertEquals(listOf(3L, 2L, 5L), all.tasks.ids())
        val due = buildMainList(manual, emptyList(), emptyList(), focuses, SortOrder.DUE_SOONEST, ManualScope.ALL)
        assertEquals(listOf(3L, 5L, 2L), due.tasks.ids())
        val focusesOnly = buildMainList(manual, emptyList(), emptyList(), focuses, SortOrder.OLDEST, ManualScope.FOCUSES)
        assertEquals(listOf(2L, 5L, 3L), focusesOnly.tasks.ids())
    }
}
