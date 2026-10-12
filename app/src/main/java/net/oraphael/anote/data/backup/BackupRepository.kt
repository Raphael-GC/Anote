package net.oraphael.anote.data.backup

import android.database.SQLException
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.oraphael.anote.data.db.AnoteDatabase
import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryType
import java.io.InputStream
import java.io.OutputStream
import java.time.Clock
import java.time.Instant

enum class ImportMode { MERGE, REPLACE }

// Contagens mostradas antes de confirmar a importação.
data class ImportPreview(
    val exportedAt: Instant,
    val tasks: Int,
    val notes: Int,
    val areas: Int,
    val focuses: Int,
)

fun BackupData.preview() = ImportPreview(
    exportedAt = exportedAt,
    tasks = entries.count { it.type == EntryType.TASK },
    notes = entries.count { it.type == EntryType.NOTE },
    areas = areas.size,
    focuses = focuses.size,
)

class BackupRepository(
    private val database: AnoteDatabase,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val backupDao = database.backupDao()

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

    suspend fun readFrom(input: InputStream): BackupData {
        val text = withContext(Dispatchers.IO) { input.bufferedReader(Charsets.UTF_8).use { it.readText() } }
        return BackupJson.decode(text)
    }

    // Tudo ou nada: qualquer violação (prazo, limite de áreas, referência
    // quebrada) desfaz a importação inteira.
    suspend fun import(data: BackupData, mode: ImportMode) {
        try {
            database.withTransaction {
                when (mode) {
                    ImportMode.REPLACE -> replace(data)
                    ImportMode.MERGE -> merge(data)
                }
            }
        } catch (e: SQLException) {
            throw BackupFormatException(BackupFormatException.Reason.CORRUPTED, e)
        } catch (e: NoSuchElementException) {
            // Vínculo apontando para área ou registro que não está no arquivo.
            throw BackupFormatException(BackupFormatException.Reason.CORRUPTED, e)
        }
    }

    private suspend fun replace(data: BackupData) {
        backupDao.deleteEntryAreas()
        backupDao.deleteFocuses()
        backupDao.deleteReviews()
        backupDao.deleteEntries()
        backupDao.deleteAreas()
        backupDao.insertAreas(data.areas)
        backupDao.insertEntries(data.entries)
        backupDao.insertEntryAreas(data.entryAreas)
        backupDao.insertFocuses(data.focuses)
        backupDao.insertReviews(data.reviews)
    }

    // Mesclar: áreas casam pelo nome; um registro já existente (mesmo tipo, texto
    // e data de criação) é mantido como está; o resto entra com novos ids.
    private suspend fun merge(data: BackupData) {
        val existingAreas = backupDao.allAreas()
        val areaByName = existingAreas.associateBy { it.name.trim().lowercase() }
        var nextOrder = (existingAreas.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val areaIds = data.areas.associate { area ->
            val existing = areaByName[area.name.trim().lowercase()]
            area.id to (existing?.id ?: backupDao.insertArea(Area(name = area.name, sortOrder = nextOrder++)))
        }

        val existingEntries = backupDao.allEntries().associateBy { Triple(it.type, it.text, it.createdAt) }
        val newEntryIds = mutableSetOf<Long>()
        val entryIds = data.entries.associate { entry ->
            val existing = existingEntries[Triple(entry.type, entry.text, entry.createdAt)]
            entry.id to (existing?.id ?: backupDao.insertEntry(entry.copy(id = 0)).also(newEntryIds::add))
        }

        backupDao.insertEntryAreas(
            data.entryAreas
                .filter { entryIds[it.entryId] in newEntryIds }
                .map { EntryArea(entryIds.getValue(it.entryId), areaIds.getValue(it.areaId)) }
        )
        backupDao.insertFocuses(data.focuses.map { it.copy(entryId = entryIds.getValue(it.entryId)) })
        backupDao.insertReviews(data.reviews)
    }
}
