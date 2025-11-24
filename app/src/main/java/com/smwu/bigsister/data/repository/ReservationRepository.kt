package com.smwu.bigsister.data.repository

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

    fun getScheduledRoutinesForDate(date: String): Flow<List<ScheduledRoutineInfo>> {
        val reservationsFlow = reservationDao.getReservationsByDate(date)
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

    suspend fun addReservation(reservation: ReservationEntity) {
        reservationDao.insertReservation(reservation)
    }

    suspend fun deleteReservation(reservationId: Long) {
        reservationDao.deleteReservationById(reservationId)
    }
}