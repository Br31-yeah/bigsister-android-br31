package com.smwu.bigsister.data.repository

import android.util.Log
import androidx.room.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import com.smwu.bigsister.data.remote.RoutineDocument
import com.smwu.bigsister.data.remote.StepDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val stepDao: StepDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /** HomeViewModel 용: 내 루틴만 가져오기 */
    fun getAllRoutines(): Flow<List<RoutineEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            routineDao.getRoutinesByUserId(user.uid)
        } else {
            emptyFlow()
        }
    }

    /** LiveModeViewModel 용: 특정 루틴 상세 조회 */
    suspend fun getRoutineWithSteps(routineId: Long): RoutineWithSteps? {
        return routineDao.getRoutineWithSteps(routineId)
    }

    /** 루틴 목록 조회 (내 ID 기준) */
    fun getRoutineListWithSteps(): Flow<List<RoutineWithSteps>> {
        val user = auth.currentUser
        return if (user != null) {
            routineDao.getRoutinesWithStepsByUserId(user.uid)
        } else {
            emptyFlow()
        }
    }

    suspend fun getRoutineByIdOnce(id: Long): RoutineEntity =
        routineDao.getRoutineById(id).first()
            ?: throw IllegalStateException("Routine not found: $id")

    /** * ✅ [추가] SettingsViewModel 에러 해결용: 로컬 데이터 일괄 삭제
     * 로그아웃이나 회원탈퇴 시 호출됩니다.
     */
    suspend fun clearAllLocalData() {
        val user = auth.currentUser
        if (user != null) {
            routineDao.deleteRoutinesByUserId(user.uid)
            // 하위 step들은 RoutineEntity 삭제 시 Cascade(연쇄삭제) 되거나
            // 수동으로 지우려면 아래를 추가하세요.
        }
    }

    /* ────────────────────────────────
       💾 저장 로직 (데이터 유실 방지 및 에러 해결)
    ──────────────────────────────── */

    @Transaction
    suspend fun saveRoutineWithSteps(
        userId: String,
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {
        // 1. 로컬 DB(Room)에 루틴 저장 및 생성된 ID 획득
        val routineId = routineDao.insertRoutine(routine.copy(userId = userId))

        // 2. 해당 루틴 ID를 참조하도록 Step들의 정보 업데이트 후 저장
        stepDao.deleteStepsByRoutineId(routineId)
        val updatedSteps = steps.map { it.copy(routineId = routineId) }

        // StepDao가 List<Long>을 반환하므로 정상적으로 대입됩니다.
        val stepIds: List<Long> = stepDao.insertSteps(updatedSteps)

        try {
            // mapIndexed를 사용하여 로컬 DB의 실제 ID를 입힙니다.
            val stepsWithRealIds = updatedSteps.mapIndexed { index, step ->
                val generatedId = stepIds.getOrNull(index) ?: step.id
                step.copy(id = generatedId)
            }

            // 3. Firestore 동기화 호출
            uploadRoutineToFirestore(
                userId,
                routine.copy(id = routineId, userId = userId),
                stepsWithRealIds
            )
        } catch (e: Exception) {
            Log.e("RoutineRepository", "Firestore 업로드 실패", e)
        }

        return routineId
    }

    private suspend fun uploadRoutineToFirestore(
        userId: String,
        routine: RoutineEntity,
        steps: List<StepEntity>
    ) {
        if (userId.isBlank()) return

        // 경로: users/{userId}/routines/{routineId}
        val routineRef = firestore.collection("users")
            .document(userId)
            .collection("routines")
            .document(routine.id.toString())

        // 루틴 기본 메타데이터 저장
        routineRef.set(RoutineDocument(routine)).await()

        // 하위 'steps' 컬렉션에 각 단계를 개별 문서로 저장 (batch 사용)
        val batch = firestore.batch()
        steps.forEach { step ->
            val stepRef = routineRef.collection("steps").document(step.id.toString())
            batch.set(stepRef, StepDocument(step))
        }
        batch.commit().await()
    }

    /* ────────────────────────────────
       🗑 삭제 및 동기화
    ──────────────────────────────── */

    suspend fun deleteRoutineById(routineId: Long) {
        try {
            val routine = getRoutineByIdOnce(routineId)
            routineDao.deleteRoutineById(routine.id)
            stepDao.deleteStepsByRoutineId(routine.id)

            if (routine.userId.isNotBlank()) {
                firestore.collection("users")
                    .document(routine.userId)
                    .collection("routines")
                    .document(routine.id.toString())
                    .delete()
            }
        } catch (e: Exception) {
            Log.e("RoutineRepository", "삭제 실패", e)
        }
    }

    /** 서버 데이터를 로컬로 가져오기 (로그인 시 호출) */
    suspend fun syncWithServer(userId: String) {
        if (userId.isBlank()) return
        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("routines").get().await()

            for (doc in snapshot.documents) {
                val routineDoc = doc.toObject(RoutineDocument::class.java) ?: continue
                val routineEntity = RoutineEntity(
                    id = routineDoc.id,
                    userId = userId,
                    title = routineDoc.title,
                    createdAt = routineDoc.createdAt,
                    totalDuration = routineDoc.totalDuration,
                    isActive = routineDoc.isActive
                )
                routineDao.insertRoutine(routineEntity)

                val stepSnapshot = doc.reference.collection("steps").get().await()
                val stepEntities = stepSnapshot.documents.mapNotNull { stepDoc ->
                    val step = stepDoc.toObject(StepDocument::class.java)
                    step?.let {
                        StepEntity(
                            id = it.id,
                            routineId = routineEntity.id,
                            name = it.name,
                            baseDuration = it.baseDuration,
                            calculatedDuration = it.calculatedDuration,
                            orderIndex = it.orderIndex
                        )
                    }
                }
                stepDao.insertSteps(stepEntities)
            }
        } catch (e: Exception) {
            Log.e("RoutineRepository", "syncWithServer 실패", e)
        }
    }
}