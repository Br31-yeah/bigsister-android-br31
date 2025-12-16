package com.smwu.bigsister.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.utils.RoutineAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsByDate(date)

    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsForMonth(month)

    fun getReservationsBetweenDates(start: String, end: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsBetweenDates(start, end)

    /** 예약 추가 */
    fun addReservation(reservation: ReservationEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.addReservation(reservation)

            val startMillis = convertToMillis(reservation.date, reservation.startTime)
            val routineWithSteps = reservationRepository.getRoutineWithSteps(reservation.routineId)

            if (routineWithSteps != null) {
                RoutineAlarmScheduler.scheduleAll(
                    context = appContext,
                    routineId = reservation.routineId,
                    routineStartMillis = startMillis,
                    steps = routineWithSteps.steps
                )
            }

            onFinished()
        }
    }

    fun deleteReservation(reservationId: Long, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            val reservation = reservationRepository.getReservationById(reservationId)

            if (reservation != null) {
                RoutineAlarmScheduler.cancelAllForRoutine(
                    context = appContext,
                    routineId = reservation.routineId
                )
            }

            reservationRepository.deleteReservation(reservationId)
            onFinished()
        }
    }

    fun convertToMillis(date: String, time: String): Long {
        val localDate = LocalDate.parse(date)
        val localTime = LocalTime.parse(time)
        return localDate.atTime(localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}