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

    /** 모든 루틴 + 스텝 JOIN 목록 */
    val routinesWithSteps: Flow<List<RoutineWithSteps>> =
        routineDao.getAllRoutinesWithSteps()

    /** 특정 루틴 1개 조회 */
    fun getRoutineById(id: Long): Flow<RoutineEntity?> =
        routineDao.getRoutineById(id)

    /** 특정 루틴 + 스텝 JOIN 조회 */
    suspend fun getRoutineWithSteps(id: Long): RoutineWithSteps? =
        routineDao.getRoutineWithSteps(id)

    /** 스텝 목록 Flow로 가져오기 */
    fun getStepsForRoutine(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** 루틴 삭제 + 스텝 삭제 */
    suspend fun deleteRoutine(id: Long) {
        routineDao.deleteRoutineById(id)
        stepDao.deleteStepsByRoutineId(id)
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        deleteRoutine(routine.id)
    }

    /** 루틴 + 스텝 저장 */
    @Transaction
    suspend fun saveRoutineWithSteps(
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {

        val routineId = routineDao.insertRoutine(routine)

        stepDao.deleteStepsByRoutineId(routineId)

        val updatedSteps = steps.map { step ->
            step.copy(routineId = routineId)
        }
        stepDao.insertSteps(updatedSteps)

        return routineId
    }
}