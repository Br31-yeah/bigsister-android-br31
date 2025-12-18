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
        Index("userId"),      // ✅ 추가: 유저별 조회 성능 향상
        Index("routineId"),
        Index("date")
    ]
)
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // ✅ 추가: 이 예약이 누구의 것인지 식별 (Firebase UID 저장)
    val userId: String,

    val routineId: Long,

    /** yyyy-MM-dd */
    val date: String,

    /** HH:mm 시작 시간 */
    val startTime: String,

    /** 종료 시간 (선택) */
    val endTime: String? = null,

    /** UI 표시용 루틴 제목 */
    val routineTitle: String
)