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

    @Query("SELECT * FROM completion_table WHERE routine_id = :routineId")
    fun getCompletionsByRoutineId(routineId: Int): Flow<List<CompletionEntity>>

    @Query(
        """
        SELECT * FROM completion_table 
        WHERE completed_at BETWEEN :start AND :end
        """
    )
    fun getCompletionsBetweenTime(
        start: Long,
        end: Long
    ): Flow<List<CompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity): Long
}