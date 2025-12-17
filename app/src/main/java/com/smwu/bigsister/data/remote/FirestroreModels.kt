package com.smwu.bigsister.data.remote

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity

data class RoutineDocument(
    val id: Long = 0L,
    val title: String = "",
    val createdAt: Long = 0L,
    val totalDurationMinutes: Int = 0,
    val isActive: Boolean = true
) {
    constructor(entity: RoutineEntity) : this(
        id = entity.id,
        title = entity.title,
        createdAt = entity.createdAt,
        totalDurationMinutes = entity.totalDurationMinutes,
        isActive = entity.isActive
    )
}

data class StepDocument(
    val id: Long = 0L,
    val routineId: Long = 0L,
    val orderIndex: Int = 0,
    val name: String = "",
    val baseDuration: Long = 0L,
    val calculatedDuration: Long? = null,
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

data class CompletionDocument(
    val id: Long = 0L,
    val routineId: Long? = null,
    val date: String = "",
    val totalTime: Int = 0,
    val wasLate: Boolean = false
) {
    constructor(entity: CompletionEntity) : this(
        id = entity.id,
        routineId = entity.routineId,
        date = entity.date,
        totalTime = entity.totalTime,
        wasLate = entity.wasLate
    )
}