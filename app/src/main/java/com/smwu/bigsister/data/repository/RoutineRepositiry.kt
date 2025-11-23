package com.smwu.bigsister.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val stepDao: StepDao,
    private val firestore: FirebaseFirestore
) {

    /** 로컬 Room 에서 루틴 전체 조회 */
    fun getAllRoutines(): Flow<List<RoutineEntity>> =
        routineDao.getAllRoutines()

    /** 로컬 Room 에서 해당 루틴의 스텝 목록 조회 */
    fun getStepsByRoutineId(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    /** 루틴 + 스텝 저장 (Room + Firestore 백업) */
    suspend fun upsertRoutineWithSteps(
        routine: RoutineEntity,
        steps: List<StepEntity>,
        userId: String?
    ) {
        // 1) Room 에 저장
        val routineId = routineDao.insertRoutine(routine)
        val stepsWithRoutineId = steps.map { it.copy(routineId = routineId) }

        // 기존 스텝 정리 후 다시 저장
        stepDao.deleteStepsByRoutineId(routineId)
        stepDao.insertSteps(stepsWithRoutineId)

        // 2) Firestore 백업 (로그인 유저 있을 때만)
        if (userId != null) {
            val routineDoc = mapOf(
                "id" to routineId,
                "title" to routine.title,
                "description" to routine.description,
                "totalDurationMinutes" to routine.totalDurationMinutes,
                "isActive" to routine.isActive
            )

            val routineRef = firestore
                .collection("users")
                .document(userId)
                .collection("routines")
                .document(routineId.toString())

            routineRef.set(routineDoc).await()

            val stepsRef = routineRef.collection("steps")
            firestore.runBatch { batch ->
                stepsWithRoutineId.forEach { step ->
                    val stepDoc = mapOf(
                        "id" to step.id,
                        "routineId" to step.routineId,
                        "orderIndex" to step.orderIndex,
                        "name" to step.name,
                        "duration" to step.duration,
                        "isTransport" to step.isTransport
                    )
                    val ref = stepsRef.document(step.id.toString())
                    batch.set(ref, stepDoc)
                }
            }.await()
        }
    }

    /** 루틴 삭제 (Room + Firestore 같이) */
    suspend fun deleteRoutine(routineId: Long, userId: String?) {
        // Room
        routineDao.deleteRoutineById(routineId)
        stepDao.deleteStepsByRoutineId(routineId)

        // Firestore
        if (userId != null) {
            firestore
                .collection("users")
                .document(userId)
                .collection("routines")
                .document(routineId.toString())
                .delete()
                .await()
        }
    }
}