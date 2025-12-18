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

    // ✅ 내 루틴만 조회
    @Query("SELECT * FROM routine_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRoutinesByUserId(userId: String): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_table WHERE id = :id")
    fun getRoutineById(id: Long): Flow<RoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routine_table WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    /** 루틴 + 스텝 JOIN 조회 */
    @Transaction
    @Query("SELECT * FROM routine_table WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineWithSteps(routineId: Long): RoutineWithSteps?

    // ✅ 내 루틴 + 스텝 목록 조회
    @Transaction
    @Query("SELECT * FROM routine_table WHERE userId = :userId")
    fun getRoutinesWithStepsByUserId(userId: String): Flow<List<RoutineWithSteps>>

    // ✅ 내 데이터만 삭제
    @Query("DELETE FROM routine_table WHERE userId = :userId")
    suspend fun deleteRoutinesByUserId(userId: String)
}