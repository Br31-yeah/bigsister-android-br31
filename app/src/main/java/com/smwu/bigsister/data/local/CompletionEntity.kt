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

    // 완료한 루틴 ID (통계용)
    val routineId: Long? = null,

    // 완료 시각 (timestamp, ms)
    val completedAt: Long = System.currentTimeMillis(),

    // 실제 수행 시간(분 or 초) – 팀에서 단위 합의해서 사용
    val totalTime: Int = 0,

    // 지각 여부 (엑셀 wasLate)
    val wasLate: Boolean = false,

    // 어떤 예약 실행 기록인지 (있을 수도 있고 없을 수도 있음)
    val reservationId: Long? = null,

    // 실행된 날짜 (YYYY-MM-DD)
    val date: String,

    // 예정된 시작/종료, 실제 시작/종료
    val expectedStartTime: String? = null,
    val expectedEndTime: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,

    // 정시 여부 + 지각 시간(분)
    val isOnTime: Boolean = true,
    val lateMinutes: Int = 0
)