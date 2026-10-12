package net.oraphael.anote.data

import net.oraphael.anote.data.db.AnoteDatabase
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

// Semana de segunda a domingo que contém `day`.
data class Week(val start: LocalDate, val end: LocalDate) {
    companion object {
        fun containing(day: LocalDate): Week {
            val start = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            return Week(start, start.plusDays(6))
        }
    }
}

data class WeekSummary(
    val week: Week,
    val completed: Int,
    val added: Int,
    val focusesCompleted: Int,
    val focusesTotal: Int,
)

class WeekRepository(
    private val database: AnoteDatabase,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    suspend fun summary(week: Week): WeekSummary {
        val from = week.start.atStartOfDay(zone).toInstant()
        val until = week.end.plusDays(1).atStartOfDay(zone).toInstant()
        val entryDao = database.entryDao()
        val focusDao = database.focusDao()
        return WeekSummary(
            week = week,
            completed = entryDao.countCompletedBetween(from, until),
            added = entryDao.countCreatedBetween(from, until),
            focusesCompleted = focusDao.countCompletedBetween(week.start, week.end),
            focusesTotal = focusDao.countBetween(week.start, week.end),
        )
    }
}
