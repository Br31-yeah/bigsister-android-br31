package com.smwu.bigsister.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smwu.bigsister.data.local.StepEntity
import kotlinx.coroutines.flow.Flow

/** 루틴 단계(Step) 관련 Room DAO */
@Dao
interface StepDao {

    /** 해당 루틴의 모든 스텝 Flow (UI에서 구독용) */
    @Query(
        """
        SELECT * FROM step_table 
        WHERE routineId = :routineId 
        ORDER BY orderIndex ASC, id ASC
        """
    )
    fun getStepsByRoutineId(routineId: Long): Flow<List<StepEntity>>

    /** 한 번만 즉시 가져오는 버전 (트랜잭션 이후 확인 등에서 사용) */
    @Query(
        """
        SELECT * FROM step_table 
        WHERE routineId = :routineId 
        ORDER BY orderIndex ASC, id ASC
        """
    )
    suspend fun getStepsOnceByRoutineId(routineId: Long): List<StepEntity>

    /** * 여러 스텝 한 번에 삽입
     * ✅ [수정] 반환 타입을 Unit에서 List<Long>으로 변경하여 생성된 ID들을 Repository에서 쓸 수 있게 합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>): List<Long>

    /** 단일 스텝 업데이트 (duration, memo, orderIndex 등 변경용) */
    @Update
    suspend fun updateStep(step: StepEntity)

    /** 해당 루틴의 모든 스텝 삭제 */
    @Query("DELETE FROM step_table WHERE routineId = :routineId")
    suspend fun deleteStepsByRoutineId(routineId: Long)


    /**
     * 트랜잭션으로 기존 스텝 전부 삭제 + 새 스텝들 삽입
     * - 완전히 갈아끼우는 용도 (루틴 편집 화면에서 '저장' 시 사용)
     */
    @Transaction
    suspend fun replaceStepsForRoutine(
        routineId: Long,
        newSteps: List<StepEntity>
    ): List<Long> { // ✅ [수정] 반환 타입 추가
        deleteStepsByRoutineId(routineId)
        return insertSteps(newSteps)
    }
}