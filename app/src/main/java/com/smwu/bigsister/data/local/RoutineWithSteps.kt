package com.smwu.bigsister.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** 루틴 + 하위 스텝 전체 JOIN 결과 정의한 관계 클래스 정의 */
data class RoutineWithSteps(
    @Embedded
    val routine: RoutineEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val steps: List<StepEntity>
)