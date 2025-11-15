package com.smwu.bigsister.data.local

import androidx.room.ColumnInfo
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
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routine_id"),
        Index("date")
    ]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "routine_id")
    val routineId: Int,

    // yyyy-MM-dd
    @ColumnInfo(name = "date")
    val date: String,

    // HH:mm
    @ColumnInfo(name = "start_time")
    val startTime: String
)