package net.oraphael.anote.data.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.oraphael.anote.data.db.BackupDao
import java.io.OutputStream
import java.time.Clock

class BackupRepository(
    private val backupDao: BackupDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun snapshot(): BackupData = BackupData(
        exportedAt = clock.instant(),
        areas = backupDao.allAreas(),
        entries = backupDao.allEntries(),
        entryAreas = backupDao.allEntryAreas(),
        focuses = backupDao.allFocuses(),
        reviews = backupDao.allReviews(),
    )

    suspend fun writeTo(output: OutputStream) {
        val json = BackupJson.encode(snapshot())
        withContext(Dispatchers.IO) {
            output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
        }
    }
}
