package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smwu.bigsister.data.local.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: StepEntity)

    // ✅ [추가] '루틴 저장' 시 여러 단계를 한 번에 저장하기 위한 함수
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSteps(steps: List<StepEntity>)

    @Update
    suspend fun updateStep(step: StepEntity)

    @Delete
    suspend fun deleteStep(step: StepEntity)

    @Query("SELECT * FROM step_table WHERE routine_id = :routineId ORDER BY step_order ASC")
    fun getStepsForRoutine(routineId: Int): Flow<List<StepEntity>>

    @Query("DELETE FROM step_table WHERE routine_id = :routineId")
    suspend fun deleteStepsForRoutine(routineId: Int)
}