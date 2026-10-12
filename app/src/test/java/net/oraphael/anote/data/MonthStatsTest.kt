package net.oraphael.anote.data

import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

// Agregação do dashboard com registros espalhados por setembro, outubro e novembro.
class MonthStatsTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val sep = YearMonth.of(2026, 9)
    private val oct = YearMonth.of(2026, 10)
    private val nov = YearMonth.of(2026, 11)

    private val work = Area(1, "Trabalho", 0)
    private val home = Area(2, "Casa", 1)
    private val health = Area(3, "Saúde", 2)
    private val areas = listOf(health, work, home)

    private fun at(text: String): Instant = LocalDateTime.parse(text).atZone(zone).toInstant()

    private var nextId = 1L

    private fun task(created: String, done: String?, status: EntryStatus = if (done != null) EntryStatus.DONE else EntryStatus.PENDING) =
        Entry(
            id = nextId++,
            type = EntryType.TASK,
            text = "t",
            createdAt = at(created),
            dueDate = LocalDate.parse(created.substring(0, 10)),
            completedAt = done?.let(::at),
            status = status,
        )

    private fun note(created: String) = Entry(
        id = nextId++, type = EntryType.NOTE, text = "n", createdAt = at(created), dueDate = null,
    )

    // Setembro: 1 concluída (Trabalho). Outubro: 4 concluídas, 1 descartada, 1 pendente, 1 anotação.
    // Novembro: 1 concluída criada em outubro.
    private val sepDone = task("2026-09-10T09:00", "2026-09-12T10:00")
    private val octFocusDone1 = task("2026-09-28T09:00", "2026-10-05T10:00") // segunda
    private val octFocusDone2 = task("2026-10-01T09:00", "2026-10-05T18:00")
    private val octFocusDone3 = task("2026-10-02T09:00", "2026-10-05T20:00")
    private val octExtra = task("2026-10-03T09:00", "2026-10-31T23:30") // sábado, ainda outubro no fuso local
    private val octDiscarded = task("2026-10-04T09:00", null, EntryStatus.DISCARDED)
    private val octPending = task("2026-10-06T09:00", null)
    private val octNote = note("2026-10-07T09:00")
    private val novDone = task("2026-10-20T09:00", "2026-11-02T08:00")
    private val entries = listOf(
        sepDone, octFocusDone1, octFocusDone2, octFocusDone3, octExtra,
        octDiscarded, octPending, octNote, novDone,
    )

    private val links = listOf(
        EntryArea(sepDone.id, work.id),
        EntryArea(octFocusDone1.id, work.id),
        EntryArea(octFocusDone1.id, home.id),
        EntryArea(octFocusDone2.id, home.id),
        EntryArea(octPending.id, health.id),
        EntryArea(novDone.id, health.id),
    )

    private val focuses = listOf(
        // 05/10: três focos, os três cumpridos.
        Focus(LocalDate.of(2026, 10, 5), octFocusDone1.id, completed = true),
        Focus(LocalDate.of(2026, 10, 5), octFocusDone2.id, completed = true),
        Focus(LocalDate.of(2026, 10, 5), octFocusDone3.id, completed = true),
        // 06/10: quatro focos, dois cumpridos.
        Focus(LocalDate.of(2026, 10, 6), octPending.id, completed = false),
        Focus(LocalDate.of(2026, 10, 6), octDiscarded.id, completed = false),
        Focus(LocalDate.of(2026, 10, 6), 900, completed = true),
        Focus(LocalDate.of(2026, 10, 6), 901, completed = true),
        // 07/10: só dois focos (havia poucas pendentes), os dois cumpridos.
        Focus(LocalDate.of(2026, 10, 7), 902, completed = true),
        Focus(LocalDate.of(2026, 10, 7), 903, completed = true),
        // Setembro, para não contaminar outubro.
        Focus(LocalDate.of(2026, 9, 30), sepDone.id, completed = true),
    )

    private fun stats(month: YearMonth) = buildMonthStats(month, entries, links, areas, focuses, zone)

    @Test
    fun completedCountsOnlyTasksDoneInTheMonth() {
        assertEquals(1, stats(sep).completed)
        assertEquals(4, stats(oct).completed)
        assertEquals(1, stats(nov).completed)
        assertEquals(0, stats(YearMonth.of(2026, 12)).completed)
    }

    @Test
    fun completionNearMidnightUsesLocalMonth() {
        // 31/10 às 23h30 em São Paulo já é 1º/11 em UTC.
        assertEquals(4, stats(oct).completed)
        assertEquals(1, stats(nov).completed)
        assertEquals(2, buildMonthStats(nov, entries, links, areas, focuses, ZoneId.of("UTC")).completed)
    }

    @Test
    fun byAreaFollowsAreaOrderAndCountsEachArea() {
        val byArea = stats(oct).byArea
        assertEquals(listOf("Trabalho", "Casa", "Saúde", null), byArea.map { it.area?.name })
        assertEquals(listOf(1, 2, 0, 2), byArea.map { it.count })
        // Setembro não tem tarefa sem área: nada de "Sem área".
        assertEquals(listOf(1, 0, 0), stats(sep).byArea.map { it.count })
    }

    @Test
    fun focusDaysNeedThreeOrAll() {
        val october = stats(oct)
        assertEquals(3, october.focusDays)
        // 05/10 (3 de 3) e 07/10 (2 de 2) contam; 06/10 (2 de 4) não.
        assertEquals(2, october.focusDaysFulfilled)
        assertEquals(1, stats(sep).focusDaysFulfilled)
    }

    @Test
    fun extrasAreCompletedTasksThatWereNotThatDaysFocus() {
        assertEquals(1, stats(oct).extras)
        // Em setembro a tarefa foi foco em 30/09, mas concluída em 12/09: conta como extra.
        assertEquals(1, stats(sep).extras)
    }

    @Test
    fun weekdayDistribution() {
        val october = stats(oct)
        // Criadas em outubro: 01 (qui), 02 (sex), 03 (sáb), 04 (dom), 06 (ter), 07 (qua), 20 (ter).
        assertEquals(listOf(0, 2, 1, 1, 1, 1, 1), october.addedByWeekday)
        // Concluídas: três na segunda 05/10 e uma no sábado 31/10.
        assertEquals(listOf(3, 0, 0, 0, 0, 1, 0), october.completedByWeekday)
    }
}
