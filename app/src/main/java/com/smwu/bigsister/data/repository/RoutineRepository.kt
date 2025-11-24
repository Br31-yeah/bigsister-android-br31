package com.smwu.bigsister.data.repository

import androidx.room.Transaction
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val stepDao: StepDao
) {

    /** 모든 루틴 목록 */
    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    /** 특정 루틴 1개 조회 */
    fun getRoutineById(id: Long): Flow<RoutineEntity?> =
        routineDao.getRoutineById(id)

    /** 특정 루틴 + 단계를 JOIN한 구조 */
    suspend fun getRoutineWithSteps(id: Long): RoutineWithSteps? =
        routineDao.getRoutineWithSteps(id)

    /** 스텝 목록 Flow로 가져오기 */
    fun getStepsForRoutine(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** 루틴 삭제 + 스텝 삭제 */
    suspend fun deleteRoutine(id: Long) {
        routineDao.deleteRoutineById(id)
        stepDao.deleteStepsByRoutineId(id)   // 이름도 일치
    }

    /**
     * 루틴 + 스텝 저장 (새로 저장 또는 덮어쓰기)
     */
    @Transaction
    suspend fun saveRoutineWithSteps(
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {

        // 1. 루틴 저장 후 ID 반환
        val routineId = routineDao.insertRoutine(routine)

        // 2. 기존 스텝 전체 삭제
        stepDao.deleteStepsByRoutineId(routineId)

        // 3. routineId 값을 넣은 스텝들을 새로 삽입
        val updatedSteps = steps.map { step ->
            step.copy(routineId = routineId)
        }
        stepDao.insertSteps(updatedSteps)

        return routineId
    }
}