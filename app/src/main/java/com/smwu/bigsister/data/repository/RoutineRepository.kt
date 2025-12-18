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

    /** ✅ HomeViewModel용: 내 예약 목록만 날짜별로 가져오기 */
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> {
        val user = auth.currentUser
        return if (user != null) {
            reservationDao.getReservationsForDate(date, user.uid)
        } else {
            emptyFlow()
        }
    }

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

    /** ✅ 로그아웃 및 회원탈퇴 시 로컬 데이터 일괄 삭제 */
    suspend fun clearAllLocalData() {
        val user = auth.currentUser
        if (user != null) {
            routineDao.deleteRoutinesByUserId(user.uid)
            reservationDao.deleteReservationsByUserId(user.uid)
        }
    }

    /** ✅ 예약 저장 및 Firestore 서버 업로드 */
    suspend fun saveReservation(reservation: ReservationEntity) {
        val user = auth.currentUser ?: return
        val generatedId = reservationDao.insertReservation(reservation.copy(userId = user.uid))
        try {
            val resDoc = ReservationDocument(reservation.copy(id = generatedId, userId = user.uid))
            firestore.collection("users").document(user.uid)
                .collection("reservations").document(generatedId.toString())
                .set(resDoc).await()
            Log.d("RESERVATION_SYNC", "예약 서버 저장 성공: $generatedId")
        } catch (e: Exception) {
            Log.e("RESERVATION_SYNC", "예약 서버 저장 실패", e)
        }
    }

    /** ✅ 예약 삭제 시 서버에서도 삭제 */
    suspend fun deleteReservation(reservationId: Long) {
        val user = auth.currentUser ?: return
        try {
            reservationDao.deleteReservationById(reservationId)
            firestore.collection("users").document(user.uid)
                .collection("reservations").document(reservationId.toString())
                .delete().await()
        } catch (e: Exception) {
            Log.e("RESERVATION_SYNC", "예약 서버 삭제 실패", e)
        }
    }

    /** ✅ 루틴과 단계 저장 로직 */
    @Transaction
    suspend fun saveRoutineWithSteps(
        userId: String,
        routine: RoutineEntity,
        steps: List<StepEntity>
    ): Long {
        val routineId = routineDao.insertRoutine(routine.copy(userId = userId))
        stepDao.deleteStepsByRoutineId(routineId)
        val updatedSteps = steps.map { it.copy(routineId = routineId) }
        val stepIds = stepDao.insertSteps(updatedSteps)

        try {
            val stepsWithRealIds = updatedSteps.mapIndexed { index, step ->
                step.copy(id = stepIds.getOrNull(index) ?: step.id)
            }
            uploadRoutineToFirestore(userId, routine.copy(id = routineId, userId = userId), stepsWithRealIds)
        } catch (e: Exception) {
            Log.e("RoutineRepository", "Firestore 업로드 실패", e)
        }
        return routineId
    }

    private suspend fun uploadRoutineToFirestore(userId: String, routine: RoutineEntity, steps: List<StepEntity>) {
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

    /** ✅ 서버 데이터를 로컬로 가져오기 (로그인 시 호출) */
    suspend fun syncWithServer(userId: String) {
        if (userId.isBlank()) return
        try {
            Log.d("SYNC", "서버 동기화 시작: $userId")

            // 1. 루틴 데이터 동기화
            val routineSnapshot = firestore.collection("users").document(userId)
                .collection("routines").get().await()

            for (doc in routineSnapshot.documents) {
                val routineDoc = doc.toObject(RoutineDocument::class.java) ?: continue
                val routineEntity = RoutineEntity(
                    id = routineDoc.id,
                    userId = userId,
                    title = routineDoc.title,
                    createdAt = routineDoc.createdAt,
                    totalDuration = routineDoc.totalDuration,
                    isActive = routineDoc.active
                )
                routineDao.insertRoutine(routineEntity)

                val stepSnapshot = doc.reference.collection("steps").get().await()
                val stepEntities = stepSnapshot.documents.mapNotNull { stepDoc ->
                    stepDoc.toObject(StepDocument::class.java)?.let {
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

            // 2. 예약 데이터 동기화
            val resSnapshot = firestore.collection("users").document(userId)
                .collection("reservations").get().await()

            for (resDoc in resSnapshot.documents) {
                val resData = resDoc.toObject(ReservationDocument::class.java) ?: continue
                val resEntity = ReservationEntity(
                    id = resData.id,
                    userId = userId,
                    routineId = resData.routineId,
                    date = resData.date,
                    startTime = resData.startTime,
                    endTime = resData.endTime,
                    routineTitle = resData.routineTitle
                )
                reservationDao.insertReservation(resEntity)
            }

            Log.d("SYNC", "서버 동기화 완료 (루틴 ${routineSnapshot.size()}, 예약 ${resSnapshot.size()})")
        } catch (e: Exception) {
            Log.e("RoutineRepository", "syncWithServer 실패", e)
        }
    }

    suspend fun deleteRoutineById(routineId: Long) {
        try {
            val routine = getRoutineByIdOnce(routineId)
            routineDao.deleteRoutineById(routine.id)
            stepDao.deleteStepsByRoutineId(routine.id)

            if (routine.userId.isNotBlank()) {
                firestore.collection("users").document(routine.userId)
                    .collection("routines").document(routine.id.toString())
                    .delete().await()
            }
        } catch (e: Exception) {
            Log.e("RoutineRepository", "삭제 실패", e)
        }
    }
}