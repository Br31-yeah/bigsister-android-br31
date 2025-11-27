package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smwu.bigsister.data.local.CompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity): Long

    @Query("SELECT * FROM completion_table ORDER BY completedAt DESC")
    fun getAllCompletions(): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table WHERE routineId = :routineId ORDER BY completedAt DESC")
    fun getCompletionsByRoutineId(routineId: Long): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table WHERE date = :date ORDER BY completedAt DESC")
    fun getCompletionsByDate(date: String): Flow<List<CompletionEntity>>

    @Query("""
        SELECT * FROM completion_table 
        WHERE date BETWEEN :startDate AND :endDate 
        ORDER BY completedAt DESC
    """)
    fun getCompletionsBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<CompletionEntity>>
}