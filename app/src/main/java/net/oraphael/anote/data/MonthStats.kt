package net.oraphael.anote.data

import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import java.time.YearMonth
import java.time.ZoneId

data class MonthStats(
    val month: YearMonth,
    val completed: Int,
)

private fun Entry.completedIn(month: YearMonth, zone: ZoneId): Boolean =
    type == EntryType.TASK && status == EntryStatus.DONE &&
        completedAt?.let { YearMonth.from(it.atZone(zone)) } == month

// Agregação do dashboard a partir das linhas do banco (função pura, testável).
@Suppress("UNUSED_PARAMETER")
fun buildMonthStats(
    month: YearMonth,
    entries: List<Entry>,
    links: List<EntryArea>,
    areas: List<Area>,
    focuses: List<Focus>,
    zone: ZoneId = ZoneId.systemDefault(),
): MonthStats {
    val completed = entries.filter { it.completedIn(month, zone) }
    return MonthStats(month = month, completed = completed.size)
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
