package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routine_table")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_table WHERE id = :id")
    fun getRoutineById(id: Long): Flow<RoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routine_table WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    /** 루틴 + 스텝 JOIN 조회 (실행/통계에서 사용) */
    @Transaction
    @Query("SELECT * FROM routine_table WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineWithSteps(routineId: Long): RoutineWithSteps?

    @Transaction
    @Query("SELECT * FROM routine_table")
    fun getAllRoutinesWithSteps(): Flow<List<RoutineWithSteps>>
}