package com.smwu.bigsister.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completion_table")
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // ✅ [추가] 동기화를 위해 userId 필요
    val userId: String = "",

    val routineId: Long,
    val date: String,          // "2023-10-25"
    val completedAt: Long,     // 완료 시점 timestamp

    // ✅ [수정] Int -> Long (초 단위 소요 시간)
    val totalTime: Long,

    val wasLate: Boolean
)