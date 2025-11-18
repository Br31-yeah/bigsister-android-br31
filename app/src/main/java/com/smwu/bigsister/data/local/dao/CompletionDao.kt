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
    suspend fun insertCompletion(completion: CompletionEntity)

    /**
     * [수정됨] 'completed_at'(Long) 필드로 기간 조회
     */
    @Query("SELECT * FROM completion_table WHERE completed_at >= :startDate AND completed_at <= :endDate ORDER BY completed_at DESC")
    fun getCompletionsForPeriod(startDate: Long, endDate: Long): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completion_table ORDER BY completed_at DESC")
    fun getAllCompletions(): Flow<List<CompletionEntity>>
}