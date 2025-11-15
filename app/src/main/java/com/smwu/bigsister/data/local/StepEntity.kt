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

    // 어떤 루틴에 속한 step인지
    val routineId: Long,

    // 순서
    val orderIndex: Int,

    // 스텝 이름 (예: "세수하기", "지하철 타기")
    val name: String,

    // 이 단계에 걸리는 시간(분)
    val durationMinutes: Int,

    // 일반 단계인지, 이동 단계인지를 구분
    val isMovementStep: Boolean = false,

    // ------ 이동 단계일 때만 사용하는 필드들 (nullable) ------

    // 출발지/도착지 이름 – 지도 검색 결과의 place name
    val departurePlaceName: String? = null,
    val arrivalPlaceName: String? = null,

    // 위경도 (필요시)
    val departureLat: Double? = null,
    val departureLng: Double? = null,
    val arrivalLat: Double? = null,
    val arrivalLng: Double? = null,

    // 이동 수단: "CAR", "TRANSIT", "WALK" 등
    val transportMode: String? = null,

    // API로 계산된 이동 소요 시간(분)
    val calculatedDurationMinutes: Int? = null,

    // 사용자가 직접 수정해서 덮어썼는지 여부
    val isDurationOverridden: Boolean = false
)