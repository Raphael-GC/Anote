package net.oraphael.anote.data.backup

import org.json.JSONArray
import org.json.JSONObject

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
}
