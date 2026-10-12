package net.oraphael.anote.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// A interface é só em português do Brasil.
val AppLocale: Locale = Locale.forLanguageTag("pt-BR")

private val dateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(AppLocale)

fun formatDateTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
    dateTimeFormatter.format(instant.atZone(zone))
