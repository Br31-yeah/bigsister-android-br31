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

    /** ViewModel용 suspend (1회 조회) */
    suspend fun getStepsByRoutineOnce(routineId: Long): List<StepEntity> =
        stepDao.getStepsByRoutineId(routineId).first()

    /** ✅ 전체 소요 시간 합산 (Long) */
    fun calculateTotalDuration(routineId: Long): Flow<Long> =
        getStepsByRoutine(routineId)
            .map { steps ->
                steps.sumOf { it.duration }
            }

    /** ✅ suspend 버전 (HomeViewModel에서 쓰기 좋음) */
    suspend fun calculateTotalDurationOnce(routineId: Long): Long =
        getStepsByRoutineOnce(routineId)
            .sumOf { it.duration }
}