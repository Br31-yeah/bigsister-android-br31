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
    private val auth: FirebaseAuth // ✅ [추가] 유저 ID 확인용
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
    // TODO: ReservationDao도 getReservationsForDateByUserId 처럼 userId 필터링이 필요할 수 있습니다.
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForDate(date)

    /** * 날짜별 루틴 + 예약 JOIN
     * ✅ [수정] 로그인한 유저의 루틴 목록만 가져와서 결합합니다.
     */
    fun getScheduledRoutinesForDate(date: String): Flow<List<ScheduledRoutineInfo>> {
        val currentUser = auth.currentUser

        // 로그인이 안 되어 있다면 빈 데이터 반환
        if (currentUser == null) return emptyFlow()

        val reservationsFlow = reservationDao.getReservationsForDate(date)
        // ❌ 기존: routineDao.getAllRoutines() -> 삭제됨
        // ✅ 수정: 내 유저 ID로 된 루틴만 가져오기
        val routinesFlow = routineDao.getRoutinesByUserId(currentUser.uid)

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

    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForMonth(month)

    fun getReservationsBetweenDates(start: String, end: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsBetweenDates(start, end)

    suspend fun addReservation(reservation: ReservationEntity) {
        // 예약 데이터에도 userId가 필요하다면 여기서 넣어줘야 합니다.
        // val uid = auth.currentUser?.uid ?: return
        // reservationDao.insertReservation(reservation.copy(userId = uid))
        reservationDao.insertReservation(reservation)
        Log.d("RESERVATION", "Saved date = ${reservation.date}")
    }

    suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservationById(reservationId)
    }
}