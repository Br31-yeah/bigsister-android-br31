package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "step_table",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId")
    ]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 소속된 루틴 ID (FK)
    val routineId: Long,

    // 단계 아이콘 (이모지 등)
    val icon: String = "✅",

    // 단계 이름 (예: "샤워하기")
    val name: String,

    // 예상 소요 시간(분)
    val duration: Int,

    // 단계 메모 (선택)
    val memo: String? = null,

    // 이동 단계 여부
    val isTransport: Boolean = false,

    // 출발지 / 도착지
    val from: String? = null,
    val to: String? = null,

    // 이동 수단: "car", "transit", "walk" 등
    val transportMode: String? = null,

    // 자동 계산된 이동 시간(분)
    val calculatedDuration: Int? = null,

    // ------------------- 추가 필드 (엑셀에는 없지만 유지) -------------------

    // 루틴 내에서의 순서 (0, 1, 2, …)
    val orderIndex: Int = 0,

    // 위경도 (필요 시 사용)
    val departureLat: Double? = null,
    val departureLng: Double? = null,
    val arrivalLat: Double? = null,
    val arrivalLng: Double? = null,

    // 사용자가 직접 시간 덮어썼는지 여부
    val isDurationOverridden: Boolean = false
)