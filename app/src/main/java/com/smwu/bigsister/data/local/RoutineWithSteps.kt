package com.smwu.bigsister.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 'RoutineEntity' 1개와 그에 속한 'StepEntity' N개를
 * 한 번의 쿼리로 가져오기 위한 데이터 클래스입니다.
 */
data class RoutineWithSteps(
    @Embedded
    val routine: RoutineEntity,

    @Relation(
        parentColumn = "id", // RoutineEntity의 Primary Key (id)
        entityColumn = "routine_id" // StepEntity의 Foreign Key (routine_id)
    )
    val steps: List<StepEntity>
)