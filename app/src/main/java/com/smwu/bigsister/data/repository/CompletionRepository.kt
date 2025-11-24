package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.dao.CompletionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompletionRepository @Inject constructor(
    private val completionDao: CompletionDao
) {

    fun getAllCompletions(): Flow<List<CompletionEntity>> =
        completionDao.getAllCompletions()

    fun getCompletionsByRoutineId(routineId: Long): Flow<List<CompletionEntity>> =
        completionDao.getCompletionsByRoutineId(routineId)

    fun getCompletionsByDate(date: String): Flow<List<CompletionEntity>> =
        completionDao.getCompletionsByDate(date)

    fun getCompletionsBetweenDates(startDate: String, endDate: String): Flow<List<CompletionEntity>> =
        completionDao.getCompletionsBetweenDates(startDate, endDate)

    suspend fun insertCompletion(completion: CompletionEntity): Long =
        completionDao.insertCompletion(completion)

    suspend fun getAllAsList(): List<CompletionEntity> =
        completionDao.getAllCompletions().first()
}
