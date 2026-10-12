package net.oraphael.anote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDate

// Foco (seção 5): um registro por foco escolhido para o dia. Alimenta o dashboard.
@Entity(
    tableName = "focuses",
    primaryKeys = ["date", "entry_id"],
    foreignKeys = [
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entry_id")],
)
data class Focus(
    val date: LocalDate,
    @ColumnInfo(name = "entry_id") val entryId: Long,
    val completed: Boolean = false,
)
