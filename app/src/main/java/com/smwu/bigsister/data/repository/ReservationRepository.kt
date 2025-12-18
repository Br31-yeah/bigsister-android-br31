package com.smwu.bigsister.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao,
    private val routineDao: RoutineDao,
    private val auth: FirebaseAuth
) {

    data class ScheduledRoutineInfo(
        val reservationId: Long,
        val routineId: Long,
        val routineTitle: String,
        val date: String,
        val startTime: String
    )

    /** 예약 1개 조회 */
    suspend fun getReservationById(id: Long): ReservationEntity? =
        reservationDao.getReservationById(id)

    /** 특정 루틴 + 스텝 JOIN 조회 */
    suspend fun getRoutineWithSteps(routineId: Long) =
        routineDao.getRoutineWithSteps(routineId)

    /** 날짜별 예약 목록 */
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> {
        val uid = auth.currentUser?.uid ?: return emptyFlow()
        return reservationDao.getReservationsForDate(date, uid)
    }

    /** * ✅ [추가] 월별 예약 조회 (ViewModel 에러 해결용)
     */
    fun getReservationsForMonth(month: String, userId: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForMonth(month, userId)

    /** * ✅ [추가] 기간별 예약 조회 (ViewModel 에러 해결용)
     */
    fun getReservationsBetweenDates(start: String, end: String, userId: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsBetweenDates(start, end, userId)

    /** 날짜별 루틴 + 예약 JOIN */
    fun getScheduledRoutinesForDate(date: String): Flow<List<ScheduledRoutineInfo>> {
        val currentUser = auth.currentUser
        if (currentUser == null) return emptyFlow()

        val userId = currentUser.uid
        val reservationsFlow = reservationDao.getReservationsForDate(date, userId)
        val routinesFlow = routineDao.getRoutinesByUserId(userId)

        return combine(reservationsFlow, routinesFlow) { reservations, routines ->
            reservations.mapNotNull { reservation ->
                val routine = routines.find { it.id == reservation.routineId }
                routine?.let {
                    ScheduledRoutineInfo(
                        reservationId = reservation.id,
                        routineId = it.id,
                        routineTitle = it.title,
                        date = reservation.date,
                        startTime = reservation.startTime
                    )
                }
            }
        }
    }

    /** 예약 추가 */
    suspend fun addReservation(reservation: ReservationEntity) {
        val uid = auth.currentUser?.uid ?: return
        reservationDao.insertReservation(reservation.copy(userId = uid))
        Log.d("RESERVATION", "Saved for user $uid on date ${reservation.date}")
    }

    suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservationById(reservationId)
    }
}