package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity) // ✅ '삭제' 기능용 함수 추가

    @Query("SELECT * FROM routine_table ORDER BY created_at DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routine_table WHERE id = :id")
    suspend fun getRoutineById(id: Int): RoutineEntity?

    /**
     * ✅ [추가] '루틴 탭'에서 루틴과 하위 단계를 함께 불러오기 위한 쿼리
     */
    @Transaction // 2개 테이블을 조회하므로 트랜잭션 보장
    @Query("SELECT * FROM routine_table ORDER BY created_at DESC")
    fun getRoutinesWithSteps(): Flow<List<RoutineWithSteps>>
}