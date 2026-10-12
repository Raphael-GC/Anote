package net.oraphael.anote.data.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

class NextOccurrenceTest {
    private val nine = LocalTime.of(21, 0)

    @Test
    fun laterTodayWhenTimeHasNotPassed() {
        val now = LocalDateTime.of(2026, 10, 12, 18, 30)
        assertEquals(LocalDateTime.of(2026, 10, 12, 21, 0), nextOccurrence(now, nine))
    }

    @Test
    fun tomorrowWhenTimeHasPassedOrIsNow() {
        assertEquals(
            LocalDateTime.of(2026, 10, 13, 21, 0),
            nextOccurrence(LocalDateTime.of(2026, 10, 12, 21, 0), nine),
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 13, 21, 0),
            nextOccurrence(LocalDateTime.of(2026, 10, 12, 22, 15), nine),
        )
    }

    @Test
    fun weeklyOnTheGivenDay() {
        val seven = LocalTime.of(19, 0)
        // 12/10/2026 é uma segunda-feira.
        assertEquals(
            LocalDateTime.of(2026, 10, 18, 19, 0),
            nextOccurrence(LocalDateTime.of(2026, 10, 12, 10, 0), seven, DayOfWeek.SUNDAY),
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 25, 19, 0),
            nextOccurrence(LocalDateTime.of(2026, 10, 18, 19, 30), seven, DayOfWeek.SUNDAY),
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 18, 19, 0),
            nextOccurrence(LocalDateTime.of(2026, 10, 18, 8, 0), seven, DayOfWeek.SUNDAY),
        )
    }
}
