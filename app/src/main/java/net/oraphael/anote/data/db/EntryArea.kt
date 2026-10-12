package net.oraphael.anote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// RegistroArea (seção 5). O limite de 3 áreas por registro fica no repositório.
@Entity(
    tableName = "entry_areas",
    primaryKeys = ["entry_id", "area_id"],
    foreignKeys = [
        ForeignKey(
            entity = Entry::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Area::class,
            parentColumns = ["id"],
            childColumns = ["area_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("area_id")],
)
data class EntryArea(
    @ColumnInfo(name = "entry_id") val entryId: Long,
    @ColumnInfo(name = "area_id") val areaId: Long,
)
