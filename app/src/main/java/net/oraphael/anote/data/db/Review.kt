package net.oraphael.anote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.Instant
import java.time.LocalDate

enum class ReviewType { DAILY, SUNDAY }

@Entity(tableName = "reviews", primaryKeys = ["type", "date"])
data class Review(
    val type: ReviewType,
    val date: LocalDate,
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,
)
