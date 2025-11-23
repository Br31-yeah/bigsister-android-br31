package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reservation_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index("date")
    ]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val routineId: Long,
    // YYYY-MM-DD
    val date: String,
    // "HH:mm"
    val startTime: String,

    val endTime: String,
    // "SCHEDULED", "COMPLETED", "CANCELLED" 등
    val status: String = "SCHEDULED"
)