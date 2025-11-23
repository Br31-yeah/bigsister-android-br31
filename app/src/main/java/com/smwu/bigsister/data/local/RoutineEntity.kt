package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 루틴 이름 (예: "아침 준비 루틴")
    val title: String,

    // 엑셀 스펙: 루틴 생성 시각 (timestamp)
    val createdAt: Long = System.currentTimeMillis(),

    // 루틴 설명 (선택)
    val description: String? = null,

    // 카테고리/색상 코드 (통계/UI용)
    val categoryColor: String? = null,

    // 예상 전체 소요 시간(분) – steps 합산 결과를 캐싱
    val totalDurationMinutes: Int = 0,

    // 활성화 여부 (루틴 탭에서 on/off)
    val isActive: Boolean = true
)