package net.oraphael.anote.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.oraphael.anote.data.db.AnoteDatabase
import org.junit.Assert.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

// Banco em memória com os mesmos gatilhos do banco real.
fun inMemoryDatabase(): AnoteDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AnoteDatabase::class.java)
        .addCallback(AnoteDatabase.callback)
        .allowMainThreadQueries()
        .build()

class MutableClock(var now: Instant = Instant.parse("2026-10-12T12:00:00Z")) : Clock() {
    override fun getZone() = ZoneOffset.UTC
    override fun withZone(zone: ZoneId) = this
    override fun instant() = now
}

inline fun <reified T : Throwable> assertFailsWith(noinline block: suspend () -> Unit): T =
    assertThrows(T::class.java) { runBlocking { block() } }
