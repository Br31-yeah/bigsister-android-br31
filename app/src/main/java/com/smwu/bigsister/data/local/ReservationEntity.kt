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

    // 어떤 루틴 템플릿을 예약한 건지
    val routineId: Long,

    // YYYY-MM-DD 형식 (예: "2025-11-15")
    val date: String,

    // 시작/끝 시간 – "HH:mm" 형식 권장 (예: "07:30")
    val startTime: String,
    val endTime: String,

    // 예약 상태: "SCHEDULED", "COMPLETED", "CANCELLED" 등
    val status: String = "SCHEDULED"
)