package net.oraphael.anote.data.reminders

import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DueNoticeTest {
    private val today = LocalDate.of(2026, 10, 12)
    private var id = 1L

    private fun task(due: LocalDate, status: EntryStatus = EntryStatus.PENDING) =
        Entry(id++, EntryType.TASK, "t", Instant.EPOCH, due, status = status)

    @Test
    fun countsPendingTasksDueTodayAndOverdue() {
        val entries = listOf(
            task(today),
            task(today),
            task(today.minusDays(3)),
            task(today.plusDays(1)),
            task(today, EntryStatus.DONE),
            task(today.minusDays(1), EntryStatus.DISCARDED),
            Entry(id++, EntryType.NOTE, "n", Instant.EPOCH, null),
        )
        assertEquals(DueCounts(today = 2, overdue = 1), dueCounts(entries, today))
    }

    @Test
    fun nothingDueMeansNoNotice() {
        assertTrue(dueCounts(listOf(task(today.plusDays(2))), today).isEmpty)
    }
}
