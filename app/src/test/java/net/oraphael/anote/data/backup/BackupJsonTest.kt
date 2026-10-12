package net.oraphael.anote.data.backup

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

// Validação do arquivo, em especial do schemaVersion.
@RunWith(RobolectricTestRunner::class)
class BackupJsonTest {
    private val valid = BackupJson.encode(
        BackupData(Instant.parse("2026-10-12T12:00:00Z"), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )

    private fun withField(key: String, value: Any?) = JSONObject(valid).put(key, value).toString()

    private fun without(key: String) = JSONObject(valid).apply { remove(key) }.toString()

    private fun reasonFor(text: String) =
        assertThrows(BackupFormatException::class.java) { BackupJson.decode(text) }.reason

    @Test
    fun acceptsCurrentVersion() {
        assertEquals(Instant.parse("2026-10-12T12:00:00Z"), BackupJson.decode(valid).exportedAt)
    }

    @Test
    fun writesCurrentSchemaVersion() {
        assertEquals(BackupJson.SCHEMA_VERSION, JSONObject(valid).getInt("schemaVersion"))
    }

    @Test
    fun rejectsMissingVersion() {
        assertEquals(BackupFormatException.Reason.MISSING_VERSION, reasonFor(without("schemaVersion")))
    }

    @Test
    fun rejectsNewerVersion() {
        assertEquals(
            BackupFormatException.Reason.NEWER_VERSION,
            reasonFor(withField("schemaVersion", BackupJson.SCHEMA_VERSION + 1))
        )
    }

    @Test
    fun rejectsInvalidVersion() {
        assertEquals(BackupFormatException.Reason.UNSUPPORTED_VERSION, reasonFor(withField("schemaVersion", 0)))
        assertEquals(BackupFormatException.Reason.UNSUPPORTED_VERSION, reasonFor(withField("schemaVersion", "um")))
    }

    @Test
    fun rejectsFilesFromOtherApps() {
        assertEquals(BackupFormatException.Reason.NOT_A_BACKUP, reasonFor("não é json"))
        assertEquals(BackupFormatException.Reason.NOT_A_BACKUP, reasonFor(withField("app", "outro.app")))
        assertEquals(BackupFormatException.Reason.NOT_A_BACKUP, reasonFor(without("app")))
    }

    @Test
    fun rejectsDamagedContent() {
        assertEquals(BackupFormatException.Reason.CORRUPTED, reasonFor(without("entries")))
        assertEquals(BackupFormatException.Reason.CORRUPTED, reasonFor(withField("exportedAt", "ontem")))
    }
}
