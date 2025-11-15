package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smwu.bigsister.data.local.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query(
        """
        SELECT * FROM step_table 
        WHERE routine_id = :routineId 
        ORDER BY id ASC
        """
    )
    fun getStepsByRoutineId(routineId: Int): Flow<List<StepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM step_table WHERE routine_id = :routineId")
    suspend fun deleteStepsByRoutineId(routineId: Int)
}