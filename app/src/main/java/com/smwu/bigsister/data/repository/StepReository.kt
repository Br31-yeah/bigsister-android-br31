package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao
) {

    fun getStepsByRoutine(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** ViewModel용 suspend */
    suspend fun getStepsByRoutineOnce(routineId: Long): List<StepEntity> =
        stepDao.getStepsByRoutineId(routineId).first()

    fun calculateTotalDuration(routineId: Long): Flow<Int> =
        getStepsByRoutine(routineId).map { it.sumOf { step -> step.duration } }
}