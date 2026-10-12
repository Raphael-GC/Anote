package net.oraphael.anote.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

// Data de hoje que muda sozinha à meia-noite: com o app aberto na virada do
// dia, a lista passa a usar os focos e as regras de ordem do novo dia.
@Composable
fun rememberToday(): State<LocalDate> = produceState(LocalDate.now()) {
    while (true) {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        delay(Duration.between(now, nextMidnight).toMillis() + 1_000)
        value = LocalDate.now()
    }
}
