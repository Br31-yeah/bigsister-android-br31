package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_table",
    foreignKeys = [
        ForeignKey(
            entity = ReservationEntity::class,
            parentColumns = ["id"],
            childColumns = ["reservationId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("reservationId"),
        Index("routineId"),
        Index("date")
    ]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 어떤 예약/루틴 실행 기록인지 (둘 중 하나만 있어도 되게 nullable)
    val reservationId: Long? = null,
    val routineId: Long? = null,

    // 실행된 날짜 (YYYY-MM-DD)
    val date: String,

    // 예정된 시작/종료, 실제 시작/종료 – ISO 문자열 or "HH:mm"
    val expectedStartTime: String? = null,
    val expectedEndTime: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,

    // 지각 여부 + 지각 시간(분)
    val isOnTime: Boolean = true,
    val lateMinutes: Int = 0
)