package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.local.AppDatabase
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 루틴 단계(Step)를 관리하는 Repository (Room 전용) */
@Singleton
class StepRepository @Inject constructor(
    private val db: AppDatabase
) {
    private val stepDao: StepDao = db.stepDao()

    /** 해당 루틴의 모든 스텝 Flow */
    fun getStepsByRoutineId(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** 해당 루틴의 총 예상 소요시간(분) Flow */
    fun getTotalDurationFlow(routineId: Long): Flow<Int> =
        getStepsByRoutineId(routineId).map { steps ->
            steps.sumOf { it.duration }
        }

    /**
     * 단순 삽입 (기존 스텝을 건드리지 않고 추가만 할 때 사용할 수 있음)
     */
    suspend fun saveSteps(steps: List<StepEntity>) {
        stepDao.insertSteps(steps)
    }

    /**
     * 해당 루틴의 스텝을 '완전히 갈아끼우는' 함수.
     *
     * - 기존 스텝 모두 삭제 후 → 새 스텝들 삽입
     * - orderIndex는 현재 리스트 순서대로 0,1,2,... 로 재할당
     * - id는 0L 로 리셋하여 Room이 새 PK 부여
     * - 최종적으로 DB에 저장된 스텝 리스트(새 PK 포함)를 반환
     */
    suspend fun replaceStepsForRoutine(
        routineId: Long,
        steps: List<StepEntity>
    ): List<StepEntity> {
        // 저장용으로 정규화 (routineId 세팅, orderIndex 재정렬, id 0으로 초기화)
        val normalized = steps.mapIndexed { index, step ->
            step.copy(
                id = 0L,
                routineId = routineId,
                orderIndex = index
            )
        }

        // DAO 트랜잭션 호출
        stepDao.replaceStepsForRoutine(routineId, normalized)

        // Room이 부여한 실제 PK(id)까지 포함한 최종 데이터 다시 조회
        return stepDao.getStepsOnceByRoutineId(routineId)
    }

    /** 해당 루틴의 모든 스텝 삭제 */
    suspend fun deleteStepsByRoutineId(routineId: Long) {
        stepDao.deleteStepsByRoutineId(routineId)
    }
}