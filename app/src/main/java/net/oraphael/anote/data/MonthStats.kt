package net.oraphael.anote.data

import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

// `area` nulo: tarefas concluídas sem nenhuma área.
data class AreaCount(val area: Area?, val count: Int)

data class MonthStats(
    val month: YearMonth,
    val completed: Int,
    // Todas as áreas, na ordem do onboarding; uma tarefa conta em cada uma das suas.
    val byArea: List<AreaCount>,
    // Dias com focos em que pelo menos 3 (ou todos, se eram menos) foram cumpridos.
    val focusDaysFulfilled: Int,
    val focusDays: Int,
    // Concluídas que não eram foco do dia em que foram concluídas.
    val extras: Int,
    // Por dia da semana, de segunda (0) a domingo (6).
    val addedByWeekday: List<Int>,
    val completedByWeekday: List<Int>,
)

private fun Entry.completedIn(month: YearMonth, zone: ZoneId): Boolean =
    type == EntryType.TASK && status == EntryStatus.DONE &&
        completedAt?.let { YearMonth.from(it.atZone(zone)) } == month

// Agregação do dashboard a partir das linhas do banco (função pura, testável).
fun buildMonthStats(
    month: YearMonth,
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    focuses: List<Focus>,
    zone: ZoneId = ZoneId.systemDefault(),
): MonthStats {
    val completed = entries.filter { it.completedIn(month, zone) }
    val completedIds = completed.map { it.id }.toSet()
    val areaIdsByEntry = links.filter { it.entryId in completedIds }.groupBy({ it.entryId }, { it.areaId })
    val countByArea = areaIdsByEntry.values.flatten().groupingBy { it }.eachCount()
    val withoutArea = completedIds.count { areaIdsByEntry[it].isNullOrEmpty() }
    val byArea = areas.sortedBy { it.sortOrder }.map { AreaCount(it, countByArea[it.id] ?: 0) } +
        listOfNotNull(AreaCount(null, withoutArea).takeIf { withoutArea > 0 })
    val focusesByDay = focuses.filter { YearMonth.from(it.date) == month }.groupBy { it.date }
    val fulfilled = focusesByDay.values.count { day ->
        day.count { it.completed } >= minOf(MIN_FOCUSES, day.size)
    }
    val focusKeys = focuses.map { it.date to it.entryId }.toSet()
    val extras = completed.count { entry ->
        val day = LocalDate.ofInstant(requireNotNull(entry.completedAt), zone)
        (day to entry.id) !in focusKeys
    }
    fun byWeekday(days: List<LocalDate>) = List(7) { i -> days.count { it.dayOfWeek.value == i + 1 } }
    val added = entries.map { LocalDate.ofInstant(it.createdAt, zone) }.filter { YearMonth.from(it) == month }
    return MonthStats(
        month = month,
        completed = completed.size,
        byArea = byArea,
        focusDaysFulfilled = fulfilled,
        focusDays = focusesByDay.size,
        extras = extras,
        addedByWeekday = byWeekday(added),
        completedByWeekday = byWeekday(completed.map { LocalDate.ofInstant(requireNotNull(it.completedAt), zone) }),
    )
}

class DashboardRepository(
    private val database: AnoteDatabase,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    suspend fun stats(month: YearMonth): MonthStats {
        val from = month.atDay(1)
        val to = month.atEndOfMonth()
        val start = from.atStartOfDay(zone).toInstant()
        val end = to.plusDays(1).atStartOfDay(zone).toInstant()
        return buildMonthStats(
            month = month,
            entries = database.entryDao().touchedBetween(start, end),
            links = database.backupDao().allEntryAreas(),
            areas = database.backupDao().allAreas(),
            focuses = database.focusDao().between(from, to),
            zone = zone,
        )
    }

    // Primeiro mês com registros, para limitar a navegação para trás.
    suspend fun firstMonth(): YearMonth? =
        database.entryDao().firstCreatedAt()?.let { YearMonth.from(it.atZone(zone)) }
}
