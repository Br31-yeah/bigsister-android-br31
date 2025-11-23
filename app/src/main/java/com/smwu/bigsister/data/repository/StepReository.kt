package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    db: AppDatabase
) {
    private val stepDao: StepDao = db.stepDao()

    fun getStepsByRoutineId(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** 해당 루틴의 총 예상 소요시간(분) 계산 */
    fun getTotalDurationFlow(routineId: Long): Flow<Int> =
        getStepsByRoutineId(routineId).map { steps ->
            steps.sumOf { it.duration }
        }

    suspend fun saveSteps(steps: List<StepEntity>) {
        stepDao.insertSteps(steps)
    }

    suspend fun deleteStepsByRoutineId(routineId: Long) {
        stepDao.deleteStepsByRoutineId(routineId)
    }
}