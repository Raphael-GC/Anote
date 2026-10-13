package net.oraphael.anote.data

import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class CompletedTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private fun at(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()

    private fun done(id: Long, completed: String) = Entry(
        id = id,
        type = EntryType.TASK,
        text = "t$id",
        createdAt = at("2026-10-01T09:00"),
        dueDate = LocalDate.of(2026, 10, 1),
        completedAt = at(completed),
        status = EntryStatus.DONE,
    )

    @Test
    fun groupsByLocalDayMostRecentFirst() {
        val entries = listOf(
            done(1, "2026-10-12T08:00"),
            // 23h30 em São Paulo já é dia 13 em UTC; conta no dia 12, no fuso local.
            done(2, "2026-10-12T23:30"),
            done(3, "2026-10-13T10:00"),
            done(4, "2026-10-10T15:00"),
        )
        val days = groupCompleted(entries, emptyList(), emptyList(), zone)

        assertEquals(
            listOf(LocalDate.of(2026, 10, 13), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 10)),
            days.map { it.date },
        )
        assertEquals(listOf(2L, 1L), days[1].entries.map { it.entry.id })
    }

    @Test
    fun keepsAreasInTheirOrder() {
        val work = Area(1, "Trabalho", 0)
        val home = Area(2, "Casa", 1)
        val days = groupCompleted(
            listOf(done(1, "2026-10-12T08:00")),
            listOf(EntryArea(1, 2), EntryArea(1, 1)),
            listOf(home, work),
            zone,
        )
        assertEquals(listOf("Trabalho", "Casa"), days.single().entries.single().areas.map { it.name })
    }
}
