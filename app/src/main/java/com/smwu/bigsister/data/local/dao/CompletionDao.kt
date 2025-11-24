package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smwu.bigsister.data.local.CompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {

    @Query("SELECT * FROM completion_table")
    fun getAllCompletions(): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table WHERE routineId = :routineId")
    fun getCompletionsByRoutineId(routineId: Long): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table WHERE date = :date")
    fun getCompletionsByDate(date: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table WHERE date BETWEEN :startDate AND :endDate")
    fun getCompletionsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<CompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity): Long
}