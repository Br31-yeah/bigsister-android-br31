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

    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    /** ✅ ViewModel에서 쓰는 함수 */
    fun getRoutineListWithSteps(): Flow<List<RoutineWithSteps>> =
        routineDao.getAllRoutinesWithSteps()

    /** 단일 루틴 (suspend 로 변경) */
    suspend fun getRoutineByIdOnce(id: Long): RoutineEntity =
        routineDao.getRoutineById(id).first()
            ?: throw IllegalStateException("Routine not found: $id")

    suspend fun getRoutineWithSteps(id: Long): RoutineWithSteps? =
        routineDao.getRoutineWithSteps(id)

    fun getStepsForRoutine(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    suspend fun deleteRoutine(id: Long) {
        routineDao.deleteRoutineById(id)
        stepDao.deleteStepsByRoutineId(id)
    }

    suspend fun deleteRoutine(routine: RoutineEntity) {
        deleteRoutine(routine.id)
    }

    @Transaction
    suspend fun saveRoutineWithSteps(
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {
        val routineId = routineDao.insertRoutine(routine)

        stepDao.deleteStepsByRoutineId(routineId)
        stepDao.insertSteps(
            steps.map { it.copy(routineId = routineId) }
        )

        return routineId
    }
}