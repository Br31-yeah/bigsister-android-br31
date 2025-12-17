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
    private val auth: FirebaseAuth // ✅ [추가] 유저 ID 확인용
) {

    // ✅ [수정] 로그인한 유저의 루틴만 가져오기
    fun getAllRoutines(): Flow<List<RoutineEntity>> {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            routineDao.getRoutinesByUserId(currentUser.uid)
        } else {
            emptyFlow() // 로그아웃 상태면 빈 데이터 반환
        }
    }

    // ✅ [수정] 로그인한 유저의 루틴(스텝 포함)만 가져오기
    fun getRoutineListWithSteps(): Flow<List<RoutineWithSteps>> {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            routineDao.getRoutinesWithStepsByUserId(currentUser.uid)
        } else {
            emptyFlow()
        }
    }

    suspend fun getRoutineByIdOnce(id: Long): RoutineEntity =
        routineDao.getRoutineById(id).first()
            ?: throw IllegalStateException("Routine not found: $id")

    suspend fun getRoutineWithSteps(id: Long): RoutineWithSteps? =
        routineDao.getRoutineWithSteps(id)

    fun getStepsForRoutine(routineId: Long): Flow<List<StepEntity>> =
        stepDao.getStepsByRoutineId(routineId)

    // ──────────────────────────────────────────────
    // ✅ 저장 로직
    // ──────────────────────────────────────────────
    @Transaction
    suspend fun saveRoutineWithSteps(
        userId: String, // ViewModel에서 넘겨주거나, 여기서 auth.currentUser?.uid 써도 됨
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {
        // 1. 로컬 저장 (userId 포함)
        val routineWithUser = routine.copy(userId = userId)
        val routineId = routineDao.insertRoutine(routineWithUser)

        // 2. 스텝 저장 (기존 스텝 지우고 다시 씀)
        stepDao.deleteStepsByRoutineId(routineId)
        val stepsWithId = steps.map { it.copy(routineId = routineId) }
        stepDao.insertSteps(stepsWithId)

        // 3. 서버 업로드 (FireStore)
        try {
            uploadRoutineToFirestore(userId, routineWithUser.copy(id = routineId), stepsWithId)
        } catch (e: Exception) {
            Log.e("RoutineRepository", "서버 업로드 실패", e)
        }

        return routineId
    }

    private suspend fun uploadRoutineToFirestore(
        userId: String,
        routine: RoutineEntity,
        steps: List<StepEntity>
    ) {
        if (userId.isBlank()) return

        val routineRef = firestore.collection("users").document(userId)
            .collection("routines").document(routine.id.toString())

        routineRef.set(RoutineDocument(routine)).await()

        val batch = firestore.batch()
        steps.forEach { step ->
            val stepRef = routineRef.collection("steps").document(step.id.toString())
            batch.set(stepRef, StepDocument(step))
        }
        batch.commit().await()
    }

    // ──────────────────────────────────────────────
    // ✅ 동기화 로직 (서버 -> 로컬)
    // ──────────────────────────────────────────────
    suspend fun syncWithServer(userId: String) {
        if (userId.isBlank()) return

        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("routines").get().await()

            for (doc in snapshot.documents) {
                val routineDoc = doc.toObject(RoutineDocument::class.java) ?: continue

                // 서버 데이터를 로컬 Entity로 변환
                val routineEntity = RoutineEntity(
                    id = routineDoc.id,
                    userId = userId,
                    title = routineDoc.title,
                    createdAt = routineDoc.createdAt,
                    totalDuration = routineDoc.totalDuration,
                    isActive = routineDoc.isActive
                )

                routineDao.insertRoutine(routineEntity)

                // 하위 스텝들도 가져와서 저장
                val stepSnapshot = doc.reference.collection("steps").get().await()
                val stepEntities = stepSnapshot.documents.mapNotNull { stepDoc ->
                    val s = stepDoc.toObject(StepDocument::class.java)
                    s?.let {
                        StepEntity(
                            id = it.id,
                            routineId = routineEntity.id,
                            name = it.name,
                            duration = it.duration,
                            orderIndex = it.orderIndex,
                            // 필요한 경우 나머지 필드도 매핑
                        )
                    }
                }
                stepDao.insertSteps(stepEntities)
            }
        } catch (e: Exception) {
            Log.e("RoutineRepository", "동기화 실패", e)
        }
    }

    // ──────────────────────────────────────────────
    // ✅ 삭제 로직
    // ──────────────────────────────────────────────
    suspend fun deleteRoutine(routine: RoutineEntity) {
        // 로컬 삭제
        routineDao.deleteRoutineById(routine.id)
        stepDao.deleteStepsByRoutineId(routine.id)

        // 서버 삭제
        if (routine.userId.isNotBlank()) {
            try {
                firestore.collection("users").document(routine.userId)
                    .collection("routines").document(routine.id.toString())
                    .delete()
            } catch (e: Exception) {
                Log.e("RoutineRepository", "서버 삭제 실패", e)
            }
        }
    }

    suspend fun deleteRoutineById(routineId: Long) {
        try {
            val routine = getRoutineByIdOnce(routineId)
            deleteRoutine(routine)
        } catch (e: Exception) {
            Log.e("RoutineRepository", "삭제 중 오류 발생 (DB에 없는 루틴일 수 있음)", e)
            // 안전하게 로컬 ID로만이라도 삭제 시도
            routineDao.deleteRoutineById(routineId)
            stepDao.deleteStepsByRoutineId(routineId)
        }
    }

    // ──────────────────────────────────────────────
    // ✅ 로컬 데이터 정리 (로그아웃 시)
    // ──────────────────────────────────────────────
    suspend fun clearAllLocalData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 현재 로그인된 유저의 데이터만 로컬 DB에서 지움
            routineDao.deleteRoutinesByUserId(currentUser.uid)
        }
    }
}