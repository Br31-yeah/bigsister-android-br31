package com.smwu.bigsister.data.remote

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity

/**
 * Firestore에 저장되는 루틴 문서
 * ✅ 수정사항: isActive -> active 로 변경 (Firestore 필드명 일치)
 */
data class RoutineDocument(
    val id: Long = 0L,
    val userId: String = "",
    val title: String = "",
    val createdAt: Long = 0L,
    val totalDuration: Long = 0L,
    val active: Boolean = true
) {
    // Firestore deserialization을 위한 빈 생성자
    constructor() : this(0L, "", "", 0L, 0L, true)

    constructor(entity: RoutineEntity) : this(
        id = entity.id,
        userId = entity.userId,
        title = entity.title,
        createdAt = entity.createdAt,
        totalDuration = entity.totalDuration,
        active = entity.isActive
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
}

/**
 * Firestore에 저장되는 예약(홈 화면 리스트) 문서
 * ✅ 수정사항: startTime, endTime 필드 추가 및 타입 통일
 */
data class ReservationDocument(
    val id: Long = 0L,
    val userId: String = "",
    val routineId: Long = 0L,
    val date: String = "",         // yyyy-MM-dd
    val startTime: String = "",    // HH:mm
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
    constructor() : this(0L, "", null, "", 0L, false)

    constructor(entity: CompletionEntity) : this(
        id = entity.id,
        userId = entity.userId,
        routineId = entity.routineId,
        date = entity.date,
        totalTime = entity.totalTime,
        wasLate = entity.wasLate
    )
}