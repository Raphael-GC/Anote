package net.oraphael.anote.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

// Janela de "próximos dias" para o indicador de prazo.
const val SOON_DAYS = 7

sealed interface DueStatus {
    data class Overdue(val days: Int) : DueStatus
    data object Today : DueStatus
    data class Soon(val days: Int) : DueStatus
    data class Later(val date: LocalDate) : DueStatus
}

fun dueStatus(due: LocalDate, today: LocalDate): DueStatus {
    val days = ChronoUnit.DAYS.between(today, due).toInt()
    return when {
        days < 0 -> DueStatus.Overdue(-days)
        days == 0 -> DueStatus.Today
        days <= SOON_DAYS -> DueStatus.Soon(days)
        else -> DueStatus.Later(due)
    }
}

// Idade em dias corridos desde a criação, no fuso do aparelho.
fun ageInDays(createdAt: Instant, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Int =
    ChronoUnit.DAYS.between(LocalDate.ofInstant(createdAt, zone), today).toInt().coerceAtLeast(0)
