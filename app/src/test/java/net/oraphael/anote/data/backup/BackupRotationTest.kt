package net.oraphael.anote.data.backup

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class BackupRotationTest {
    private fun backup(day: Int) = backupFileName(LocalDateTime.of(2026, 10, day, 21, 0))

    @Test
    fun keepsTheSevenMostRecent() {
        val names = (1..10).map(::backup).shuffled()
        assertEquals(listOf(backup(3), backup(2), backup(1)), backupsToDelete(names))
    }

    @Test
    fun nothingToDeleteWithSevenOrLess() {
        assertEquals(emptyList<String>(), backupsToDelete((1..7).map(::backup)))
    }

    @Test
    fun ignoresOtherFiles() {
        val names = (1..8).map(::backup) + listOf("anote-2026-10-01.json", "notas.txt", "anote-backup-x.txt")
        assertEquals(listOf(backup(1)), backupsToDelete(names))
    }

    @Test
    fun fileNameSortsChronologically() {
        assertEquals("anote-backup-2026-10-12-2105.json", backupFileName(LocalDateTime.of(2026, 10, 12, 21, 5)))
    }
}
