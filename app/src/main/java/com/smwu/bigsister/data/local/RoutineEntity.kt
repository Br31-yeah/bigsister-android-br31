package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_table")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // ✅ [추가] 데이터 주인 식별용 (로그인된 유저 UID)
    val userId: String = "",

    val title: String,
    val createdAt: Long = System.currentTimeMillis(),

    // ✅ [수정] Int -> Long (초 단위 계산 대비)
    // 이름도 Minutes를 떼고 범용적인 'totalDuration'으로 변경 추천
    val totalDuration: Long = 0L,

    val isActive: Boolean = true
)