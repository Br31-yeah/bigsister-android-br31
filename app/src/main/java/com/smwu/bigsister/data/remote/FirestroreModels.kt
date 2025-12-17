package com.smwu.bigsister.data.remote

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity

/**
 * Firestore에 저장되는 루틴 문서
 * 👉 Entity 기준으로 통일 (totalDurationMinutes 제거)
 */
data class RoutineDocument(
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val totalDuration: Long = 0L,
    val isActive: Boolean = true
) {
    constructor(entity: RoutineEntity) : this(
        id = entity.id,
        userId = entity.userId,
        title = entity.title,
        createdAt = entity.createdAt,
        totalDuration = entity.totalDuration,
        isActive = entity.isActive
    )
}

/**
 * Firestore에 저장되는 루틴 스텝 문서
 */
data class StepDocument(
    val id: Long = 0L,
    val routineId: Long = 0L,
    val orderIndex: Int = 0,
    val name: String = "",
    val baseDuration: Long = 0L,
    val calculatedDuration: Long? = null
) {
    constructor(entity: StepEntity) : this(
        id = entity.id,
        routineId = entity.routineId,
        orderIndex = entity.orderIndex,
        name = entity.name,
        baseDuration = entity.baseDuration,
        calculatedDuration = entity.calculatedDuration
    )
}

/**
 * Firestore에 저장되는 완료 기록 문서
 */
data class CompletionDocument(
    val id: Long = 0L,
    val userId: String = "",
    val routineId: Long? = null,
    val date: String = "",
    val totalTime: Long = 0L,
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