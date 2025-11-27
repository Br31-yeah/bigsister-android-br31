package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao
) {
    suspend fun getStepsByRoutine(routineId: Long): List<StepEntity> {
        return stepDao.getStepsByRoutineId(routineId).first()
    }

    suspend fun calculateTotalDuration(routineId: Long): Int {
        val steps = getStepsByRoutine(routineId)
        return steps.sumOf { it.duration }
    }
}