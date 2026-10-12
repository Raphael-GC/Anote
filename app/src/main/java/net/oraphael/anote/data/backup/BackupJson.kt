package net.oraphael.anote.data.backup

import net.oraphael.anote.data.db.Area
import net.oraphael.anote.data.db.Entry
import net.oraphael.anote.data.db.EntryArea
import net.oraphael.anote.data.db.EntryStatus
import net.oraphael.anote.data.db.EntryType
import net.oraphael.anote.data.db.Focus
import net.oraphael.anote.data.db.Review
import net.oraphael.anote.data.db.ReviewType
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate

// Formato JSON de exportação. Datas em ISO-8601; `schemaVersion` sobe a cada
// mudança incompatível no formato.
object BackupJson {
    const val SCHEMA_VERSION = 1
    private const val APP_ID = "net.oraphael.anote"

    fun encode(data: BackupData): String = JSONObject()
        .put("schemaVersion", SCHEMA_VERSION)
        .put("app", APP_ID)
        .put("exportedAt", data.exportedAt.toString())
        .put("areas", JSONArray(data.areas.map {
            JSONObject().put("id", it.id).put("name", it.name).put("sortOrder", it.sortOrder)
        }))
        .put("entries", JSONArray(data.entries.map {
            JSONObject()
                .put("id", it.id)
                .put("type", it.type.name)
                .put("text", it.text)
                .put("createdAt", it.createdAt.toString())
                .put("dueDate", it.dueDate?.toString() ?: JSONObject.NULL)
                .put("completedAt", it.completedAt?.toString() ?: JSONObject.NULL)
                .put("manualPosition", it.manualPosition ?: JSONObject.NULL)
                .put("status", it.status.name)
        }))
        .put("entryAreas", JSONArray(data.entryAreas.map {
            JSONObject().put("entryId", it.entryId).put("areaId", it.areaId)
        }))
        .put("focuses", JSONArray(data.focuses.map {
            JSONObject().put("date", it.date.toString()).put("entryId", it.entryId).put("completed", it.completed)
        }))
        .put("reviews", JSONArray(data.reviews.map {
            JSONObject()
                .put("type", it.type.name)
                .put("date", it.date.toString())
                .put("completedAt", it.completedAt?.toString() ?: JSONObject.NULL)
        }))
        .toString(2)

    // Valida o arquivo antes de qualquer escrita no banco.
    fun decode(text: String): BackupData {
        val root = try {
            JSONObject(text)
        } catch (e: JSONException) {
            throw BackupFormatException(BackupFormatException.Reason.NOT_A_BACKUP)
        }
        if (root.optString("app") != APP_ID) {
            throw BackupFormatException(BackupFormatException.Reason.NOT_A_BACKUP)
        }
        if (!root.has("schemaVersion")) {
            throw BackupFormatException(BackupFormatException.Reason.MISSING_VERSION)
        }
        val version = root.optInt("schemaVersion", -1)
        when {
            version > SCHEMA_VERSION -> throw BackupFormatException(BackupFormatException.Reason.NEWER_VERSION)
            version < 1 -> throw BackupFormatException(BackupFormatException.Reason.UNSUPPORTED_VERSION)
        }
        return try {
            BackupData(
                exportedAt = Instant.parse(root.getString("exportedAt")),
                areas = root.getJSONArray("areas").objects().map {
                    Area(id = it.getLong("id"), name = it.getString("name"), sortOrder = it.getInt("sortOrder"))
                },
                entries = root.getJSONArray("entries").objects().map {
                    Entry(
                        id = it.getLong("id"),
                        type = EntryType.valueOf(it.getString("type")),
                        text = it.getString("text"),
                        createdAt = Instant.parse(it.getString("createdAt")),
                        dueDate = it.stringOrNull("dueDate")?.let(LocalDate::parse),
                        completedAt = it.stringOrNull("completedAt")?.let(Instant::parse),
                        manualPosition = if (it.isNull("manualPosition")) null else it.getInt("manualPosition"),
                        status = EntryStatus.valueOf(it.getString("status")),
                    )
                },
                entryAreas = root.getJSONArray("entryAreas").objects().map {
                    EntryArea(entryId = it.getLong("entryId"), areaId = it.getLong("areaId"))
                },
                focuses = root.getJSONArray("focuses").objects().map {
                    Focus(
                        date = LocalDate.parse(it.getString("date")),
                        entryId = it.getLong("entryId"),
                        completed = it.getBoolean("completed"),
                    )
                },
                reviews = root.getJSONArray("reviews").objects().map {
                    Review(
                        type = ReviewType.valueOf(it.getString("type")),
                        date = LocalDate.parse(it.getString("date")),
                        completedAt = it.stringOrNull("completedAt")?.let(Instant::parse),
                    )
                },
            )
        } catch (e: Exception) {
            throw BackupFormatException(BackupFormatException.Reason.CORRUPTED, e)
        }
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }

    private fun JSONObject.stringOrNull(key: String): String? = if (isNull(key)) null else getString(key)
}

class BackupFormatException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason { NOT_A_BACKUP, MISSING_VERSION, NEWER_VERSION, UNSUPPORTED_VERSION, CORRUPTED }
}
