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

    /* ────────────────────────────────
       🔁 ViewModel 호환 API (중요)
    ──────────────────────────────── */

    /** HomeViewModel 용 */
    fun getAllRoutines(): Flow<List<RoutineEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            routineDao.getRoutinesByUserId(user.uid)
        } else {
            emptyFlow()
        }
    }

    /** LiveModeViewModel 용 */
    suspend fun getRoutineWithSteps(routineId: Long): RoutineWithSteps? {
        return routineDao.getRoutineWithSteps(routineId)
    }

    /** SettingsViewModel 용 */
    suspend fun clearAllLocalData() {
        val user = auth.currentUser
        if (user != null) {
            routineDao.deleteRoutinesByUserId(user.uid)
        }
    }

    /* ────────────────────────────────
       📋 루틴 목록 (Compose 화면용)
    ──────────────────────────────── */

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

    /* ────────────────────────────────
       💾 저장
    ──────────────────────────────── */

    @Transaction
    suspend fun saveRoutineWithSteps(
        userId: String,
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {
        val routineId = routineDao.insertRoutine(routine.copy(userId = userId))

        stepDao.deleteStepsByRoutineId(routineId)
        stepDao.insertSteps(steps.map { it.copy(routineId = routineId) })

        try {
            uploadRoutineToFirestore(
                userId,
                routine.copy(id = routineId, userId = userId),
                steps.map { it.copy(routineId = routineId) }
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

        val routineRef = firestore.collection("users")
            .document(userId)
            .collection("routines")
            .document(routine.id.toString())

        routineRef.set(RoutineDocument(routine)).await()

        val batch = firestore.batch()
        steps.forEach { step ->
            val stepRef = routineRef.collection("steps").document(step.id.toString())
            batch.set(stepRef, StepDocument(step))
        }
        batch.commit().await()
    }

    /* ────────────────────────────────
       🗑 삭제
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

    /* ──────────────────────────────────────────────
   🔄 서버 → 로컬 동기화 (LoginViewModel에서 사용)
────────────────────────────────────────────── */
    suspend fun syncWithServer(userId: String) {
        if (userId.isBlank()) return

        try {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("routines")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val routineDoc = doc.toObject(RoutineDocument::class.java)
                    ?: continue

                // 루틴 저장
                val routineEntity = RoutineEntity(
                    id = routineDoc.id,
                    userId = userId,
                    title = routineDoc.title,
                    createdAt = routineDoc.createdAt,
                    totalDuration = routineDoc.totalDuration,
                    isActive = routineDoc.isActive
                )

                routineDao.insertRoutine(routineEntity)

                // 하위 step 저장
                val stepSnapshot =
                    doc.reference.collection("steps").get().await()

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