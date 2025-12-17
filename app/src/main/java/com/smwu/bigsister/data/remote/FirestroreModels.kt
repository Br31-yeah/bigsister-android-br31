package com.smwu.bigsister.data.remote

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity

// 루틴 문서
data class RoutineDocument(
    val id: Long = 0L,
    val userId: String = "", // 서버에도 저장해두면 좋음
    val title: String = "",
    val createdAt: Long = 0L,
    val totalDuration: Long = 0L, // ✅ Int -> Long 변경
    val isActive: Boolean = true
) {
    constructor(entity: RoutineEntity) : this(
        id = entity.id,
        userId = entity.userId,
        title = entity.title,
        createdAt = entity.createdAt,
        totalDuration = entity.totalDuration, // 필드명 변경 반영
        isActive = entity.isActive
    )
}

// 완료 기록 문서
data class CompletionDocument(
    val id: Long = 0L,
    val userId: String = "",
    val routineId: Long? = null,
    val date: String = "",
    val totalTime: Long = 0L, // ✅ Int -> Long 변경
    val wasLate: Boolean = false
) {
    constructor(entity: CompletionEntity) : this(
        id = entity.id,
        userId = entity.userId,
        routineId = entity.routineId,
        date = entity.date,
        totalTime = entity.totalTime,
        wasLate = entity.wasLate
    )
}

// 스텝 문서는 이미 Long이라 수정 불필요 (duration: Long)
data class StepDocument(
    val id: Long = 0L,
    val routineId: Long = 0L,
    val orderIndex: Int = 0,
    val name: String = "",
    val duration: Long = 0L
) {
    constructor(entity: StepEntity) : this(
        id = entity.id,
        routineId = entity.routineId,
        orderIndex = entity.orderIndex,
        name = entity.name,
        duration = entity.duration
    )
}