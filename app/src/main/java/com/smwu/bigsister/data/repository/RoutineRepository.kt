package com.smwu.bigsister.data.repository

import androidx.room.Transaction
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val stepDao: StepDao
) {

    val routinesWithSteps: Flow<List<RoutineWithSteps>> = routineDao.getRoutinesWithSteps()

    // ✅ [추가] ID로 'RoutineWithSteps' 1개 가져오기 (수정용)
    suspend fun getRoutineWithSteps(id: Int): RoutineWithSteps? {
        // (임시 구현. 더 효율적인 쿼리가 필요할 수 있음)
        return routinesWithSteps.first().find { it.routine.id == id }
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        routineDao.deleteRoutine(routine)
    }

    fun getStepsForRoutine(routineId: Int): Flow<List<StepEntity>> {
        return stepDao.getStepsForRoutine(routineId)
    }

    /**
     * ✅ [신규] 루틴과 단계를 한 번에 저장 (생성/수정)
     * @Transaction: 이 작업이 중간에 실패하면 모두 롤백됩니다.
     */
    @Transaction
    suspend fun saveRoutineWithSteps(routine: RoutineEntity, steps: List<StepEntity>) {
        // 1. 루틴을 저장하고 ID를 받습니다. (이미 ID가 있으면 업데이트)
        val routineId = routineDao.insertRoutine(routine)

        // 2. 모든 단계에 올바른 루틴 ID를 할당합니다.
        val stepsWithRoutineId = steps.map { step ->
            step.copy(routineId = routineId.toInt())
        }

        // 3. (수정 시) 기존 단계를 모두 삭제합니다.
        stepDao.deleteStepsForRoutine(routineId.toInt())

        // 4. 새 단계들을 모두 저장합니다.
        stepDao.insertAllSteps(stepsWithRoutineId)
    }
}