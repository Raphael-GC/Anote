package net.oraphael.anote.data.reminders

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class BackupReminderTest {
    private val now = Instant.parse("2026-10-18T19:00:00Z")

    @Test
    fun remindsWhenAutomaticBackupIsOff() {
        assertTrue(needsBackupReminder(hasFolder = false, lastBackupAt = now, now = now))
    }

    @Test
    fun remindsWhenNoBackupWasEverWritten() {
        assertTrue(needsBackupReminder(hasFolder = true, lastBackupAt = null, now = now))
    }

    @Test
    fun remindsOnlyAfterSevenDays() {
        assertFalse(needsBackupReminder(true, now.minus(Duration.ofDays(1)), now))
        assertFalse(needsBackupReminder(true, now.minus(Duration.ofDays(7)), now))
        assertTrue(needsBackupReminder(true, now.minus(Duration.ofDays(7)).minusSeconds(1), now))
    }
}
