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

    // ✅ [수정] 모든 루틴 조회 -> 내 루틴만 조회 (userId 필터 추가)
    @Query("SELECT * FROM routine_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getRoutinesByUserId(userId: String): Flow<List<RoutineEntity>>

    // ID로 개별 조회는 그대로 둠 (이미 특정 루틴을 선택한 상태이므로)
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

    // ✅ [수정] 모든 루틴+스텝 조회 -> 내 것만 조회
    @Transaction
    @Query("SELECT * FROM routine_table WHERE userId = :userId")
    fun getRoutinesWithStepsByUserId(userId: String): Flow<List<RoutineWithSteps>>

    // ✅ [수정] 모든 데이터 삭제 -> 내 데이터만 삭제 (로그아웃/탈퇴 시 사용)
    @Query("DELETE FROM routine_table WHERE userId = :userId")
    suspend fun deleteRoutinesByUserId(userId: String)
}