package com.smwu.bigsister.data.repository

import android.util.Log
import androidx.room.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.local.dao.StepDao
import com.smwu.bigsister.data.remote.ReservationDocument
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
    private val reservationDao: ReservationDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /** ✅ [추가됨] 로그아웃 시 로컬 데이터 삭제 */
    suspend fun clearAllLocalData() {
        val user = auth.currentUser
        if (user != null) {
            routineDao.deleteRoutinesByUserId(user.uid)
            reservationDao.deleteReservationsByUserId(user.uid)
        }
    }

    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> {
        val user = auth.currentUser
        return if (user != null) reservationDao.getReservationsForDate(date, user.uid) else emptyFlow()
    }

    fun getAllRoutines(): Flow<List<RoutineEntity>> {
        val user = auth.currentUser
        return if (user != null) routineDao.getRoutinesByUserId(user.uid) else emptyFlow()
    }

    suspend fun saveReservation(reservation: ReservationEntity) {
        val user = auth.currentUser ?: return
        val generatedId = reservationDao.insertReservation(reservation.copy(userId = user.uid))
        try {
            val resDoc = ReservationDocument(reservation.copy(id = generatedId, userId = user.uid))
            firestore.collection("users").document(user.uid)
                .collection("reservations").document(generatedId.toString())
                .set(resDoc).await()
        } catch (e: Exception) { Log.e("SYNC", "예약 서버 저장 실패", e) }
    }

    @Transaction
    suspend fun saveRoutineWithSteps(userId: String, routine: RoutineEntity, steps: List<StepEntity>): Long {
        val routineId = routineDao.insertRoutine(routine.copy(userId = userId))
        stepDao.deleteStepsByRoutineId(routineId)
        val updatedSteps = steps.map { it.copy(routineId = routineId) }
        val stepIds = stepDao.insertSteps(updatedSteps)
        try {
            val stepsWithRealIds = updatedSteps.mapIndexed { index, step ->
                step.copy(id = stepIds.getOrNull(index) ?: step.id)
            }
            uploadRoutineToFirestore(userId, routine.copy(id = routineId, userId = userId), stepsWithRealIds)
        } catch (e: Exception) { Log.e("SYNC", "실패", e) }
        return routineId
    }

    private suspend fun uploadRoutineToFirestore(userId: String, routine: RoutineEntity, steps: List<StepEntity>) {
        if (userId.isBlank()) return
        val routineRef = firestore.collection("users").document(userId)
            .collection("routines").document(routine.id.toString())

        // ✅ RoutineDocument 생성자에 entity를 직접 전달
        routineRef.set(RoutineDocument(routine)).await()

        val batch = firestore.batch()
        steps.forEach { step ->
            val stepRef = routineRef.collection("steps").document(step.id.toString())
            // ✅ StepDocument 생성자에 entity를 직접 전달
            batch.set(stepRef, StepDocument(step))
        }
        batch.commit().await()
    }

    suspend fun syncWithServer(userId: String) {
        if (userId.isBlank()) return
        try {
            val routineSnapshot = firestore.collection("users").document(userId).collection("routines").get().await()
            for (doc in routineSnapshot.documents) {
                val routineDoc = doc.toObject(RoutineDocument::class.java) ?: continue
                routineDao.insertRoutine(routineDoc.toEntity())
                val stepSnapshot = doc.reference.collection("steps").get().await()
                val stepEntities = stepSnapshot.documents.mapNotNull { it.toObject(StepDocument::class.java)?.toEntity() }
                stepDao.insertSteps(stepEntities)
            }
            val resSnapshot = firestore.collection("users").document(userId).collection("reservations").get().await()
            for (resDoc in resSnapshot.documents) {
                val resData = resDoc.toObject(ReservationDocument::class.java) ?: continue
                reservationDao.insertReservation(resData.toEntity())
            }
        } catch (e: Exception) { Log.e("SYNC", "서버 동기화 실패", e) }
    }

    suspend fun deleteReservation(reservationId: Long) {
        val user = auth.currentUser ?: return
        reservationDao.deleteReservationById(reservationId)
        firestore.collection("users").document(user.uid).collection("reservations").document(reservationId.toString()).delete().await()
    }

    fun getRoutineListWithSteps(): Flow<List<RoutineWithSteps>> {
        val user = auth.currentUser
        return if (user != null) routineDao.getRoutinesWithStepsByUserId(user.uid) else emptyFlow()
    }

    suspend fun getRoutineWithSteps(routineId: Long): RoutineWithSteps? = routineDao.getRoutineWithSteps(routineId)
    suspend fun getRoutineByIdOnce(id: Long): RoutineEntity = routineDao.getRoutineById(id).first() ?: throw IllegalStateException()
    suspend fun deleteRoutineById(routineId: Long) {
        val user = auth.currentUser ?: return
        routineDao.deleteRoutineById(routineId)
        firestore.collection("users").document(user.uid).collection("routines").document(routineId.toString()).delete().await()
    }
}