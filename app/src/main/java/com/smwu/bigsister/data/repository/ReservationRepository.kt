package com.smwu.bigsister.data.repository

import android.util.Log
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.dao.ReservationDao
import com.smwu.bigsister.data.local.dao.RoutineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao,
    private val routineDao: RoutineDao
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

    /** 특정 루틴 + 스텝 JOIN 조회 (알림 스케줄링에서 필요) */
    suspend fun getRoutineWithSteps(routineId: Long) =
        routineDao.getRoutineWithSteps(routineId)


    /** 날짜별 예약 목록 */
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> =
        reservationDao.getReservationsForDate(date)   // ← 여기 수정됨!

    /** 날짜별 루틴 + 예약 JOIN */
    fun getScheduledRoutinesForDate(date: String): Flow<List<ScheduledRoutineInfo>> {
        val reservationsFlow = reservationDao.getReservationsForDate(date)
        val routinesFlow = routineDao.getAllRoutines()

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
        reservationDao.insertReservation(reservation)
        Log.d("RESERVATION", "Saved date = ${reservation.date}")
    }

    suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservationById(reservationId)
    }
}