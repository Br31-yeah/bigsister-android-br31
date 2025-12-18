package com.smwu.bigsister.data.remote

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity

/**
 * Firestore에 저장되는 루틴 문서
 */
data class RoutineDocument(
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val totalDuration: Long = 0L,
    val active: Boolean = true
) {
    constructor() : this(0L, "", "", 0L, 0L, true)

    // Entity -> Document 변환 (저장 시 사용)
    constructor(entity: RoutineEntity) : this(
        id = entity.id,
        userId = entity.userId,
        title = entity.title,
        createdAt = entity.createdAt,
        totalDuration = entity.totalDuration,
        active = entity.isActive
    )

    // Document -> Entity 변환 (불러오기 시 사용)
    // ✅ 명시적 이름 지정으로 타입 미스매치 방지
    fun toEntity(): RoutineEntity = RoutineEntity(
        id = id,
        userId = userId,
        title = title,
        createdAt = createdAt,
        totalDuration = totalDuration,
        isActive = active
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
    constructor() : this(0L, 0L, 0, "", 0L, null)

    constructor(entity: StepEntity) : this(
        id = entity.id,
        routineId = entity.routineId,
        orderIndex = entity.orderIndex,
        name = entity.name,
        baseDuration = entity.baseDuration,
        calculatedDuration = entity.calculatedDuration
    )

    fun toEntity(): StepEntity = StepEntity(
        id = id,
        routineId = routineId,
        orderIndex = orderIndex,
        name = name,
        baseDuration = baseDuration,
        calculatedDuration = calculatedDuration
    )
}

/**
 * Firestore에 저장되는 예약 문서
 */
data class ReservationDocument(
    val id: Long = 0L,
    val userId: String = "",
    val routineId: Long = 0L,
    val date: String = "",
    val startTime: String = "",
    val endTime: String? = null,
    val routineTitle: String = ""
) {
    constructor() : this(0L, "", 0L, "", "", null, "")

    constructor(entity: ReservationEntity) : this(
        id = entity.id,
        userId = entity.userId,
        routineId = entity.routineId,
        date = entity.date,
        startTime = entity.startTime,
        endTime = entity.endTime,
        routineTitle = entity.routineTitle
    )

    fun toEntity(): ReservationEntity = ReservationEntity(
        id = id,
        userId = userId,
        routineId = routineId,
        date = date,
        startTime = startTime,
        endTime = endTime,
        routineTitle = routineTitle
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
    val completedAt: Long = 0L,
    val totalTime: Long = 0L,
    val wasLate: Boolean = false
) {
    constructor() : this(0L, "", null, "", 0L, 0L, false)

    // Entity -> Document
    constructor(entity: CompletionEntity) : this(
        id = entity.id,
        userId = entity.userId,
        routineId = entity.routineId,
        date = entity.date,
        completedAt = entity.completedAt,
        totalTime = entity.totalTime,
        wasLate = entity.wasLate
    )

    // Document -> Entity
    // ✅ 에러 로그의 'Long?' vs 'Boolean' 충돌은 여기서 파라미터 순서가 꼬였기 때문입니다.
    fun toEntity(): CompletionEntity = CompletionEntity(
        id = id,
        userId = userId,
        routineId = routineId ?: 0L,
        date = date,
        completedAt = completedAt,
        totalTime = totalTime,
        wasLate = wasLate
    )
}