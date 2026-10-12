package net.oraphael.anote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

enum class EntryType { TASK, NOTE }

enum class EntryStatus { PENDING, DONE, DISCARDED }

// Registro (seção 5). `createdAt` é a base da ordenação por idade e nunca muda.
@Entity(tableName = "entries", indices = [Index("status"), Index("created_at")])
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: EntryType,
    val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    // Obrigatório em tarefas, nulo em anotações.
    @ColumnInfo(name = "due_date") val dueDate: LocalDate?,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
    @ColumnInfo(name = "manual_position") val manualPosition: Int? = null,
    val status: EntryStatus = EntryStatus.PENDING,
)
