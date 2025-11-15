package com.smwu.bigsister.data.local.dao

import androidx.room.*
import com.smwu.bigsister.data.local.RoutineEntity
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
}