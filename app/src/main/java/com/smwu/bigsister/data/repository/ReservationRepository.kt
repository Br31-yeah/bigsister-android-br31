package com.smwu.bigsister.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import com.smwu.bigsister.data.remote.ReservationDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao,
    private val routineDao: RoutineDao,
    private val firestore: FirebaseFirestore
) {
    /** ✅ 로그인 시 서버 데이터를 로컬 Room으로 복구하는 함수 */
    suspend fun syncReservationsFromServer(userId: String) {
        if (userId.isBlank()) return
        withContext(Dispatchers.IO + NonCancellable) {
            try {
                Log.d("SYNC", "예약 데이터 동기화 시작: $userId")
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("reservations")
                    .get()
                    .await()

                // ✅ 타입을 명시적으로 지정하여 컴파일러의 타입 추론 에러 해결
                val remoteReservations: List<ReservationEntity> = snapshot.documents.mapNotNull { doc: DocumentSnapshot ->
                    doc.toObject(ReservationDocument::class.java)?.toEntity()
                }

                remoteReservations.forEach { reservation ->
                    reservationDao.insertReservation(reservation)
                }
                Log.d("SYNC", "예약 동기화 완료: ${remoteReservations.size}건")
            } catch (e: Exception) {
                Log.e("SYNC", "예약 동기화 실패: ${e.message}")
            }
        }
    }

    fun getReservationsByDate(date: String, userId: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForDate(date, userId)

    fun getReservationsByMonth(month: String, userId: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForMonth(month, userId)

    fun getReservationsBetweenDates(startDate: String, endDate: String, userId: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsBetweenDates(startDate, endDate, userId)

    /** ✅ 예약 추가 (로컬 저장 + 서버 업로드) */
    suspend fun addReservation(reservation: ReservationEntity) {
        val newId = reservationDao.insertReservation(reservation)
        if (reservation.userId.isNotBlank()) {
            val doc = ReservationDocument(reservation.copy(id = newId))
            firestore.collection("users")
                .document(reservation.userId)
                .collection("reservations")
                .document(newId.toString())
                .set(doc)
                .await()
        }
    }

    suspend fun deleteReservation(id: Long) = reservationDao.deleteReservationById(id)
    suspend fun getReservationById(id: Long) = reservationDao.getReservationById(id)
    suspend fun getRoutineWithSteps(routineId: Long) = routineDao.getRoutineWithSteps(routineId)
}