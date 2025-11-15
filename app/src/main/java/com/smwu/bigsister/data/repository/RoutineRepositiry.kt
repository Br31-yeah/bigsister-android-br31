package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.dao.RoutineDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao
) {

    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    fun getRoutineById(id: Int): Flow<RoutineEntity?> =
        routineDao.getRoutineById(id)

    suspend fun addRoutine(routine: RoutineEntity): Long =
        routineDao.insertRoutine(routine)

    suspend fun updateRoutine(routine: RoutineEntity) {
        routineDao.updateRoutine(routine)
    }

    suspend fun deleteRoutine(id: Int) {
        routineDao.deleteRoutineById(id)
    }
}