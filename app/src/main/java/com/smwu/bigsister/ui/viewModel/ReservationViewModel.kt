package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    /** 날짜별 예약 조회 */
    fun getReservationsForDate(date: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsByDate(date)

    /** 월별 예약 조회 */
    fun getReservationsForMonth(month: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsForMonth(month)

    /** 범위 조회 (예: 주간, 특정 기간) */
    fun getReservationsBetweenDates(start: String, end: String): Flow<List<ReservationEntity>> =
        reservationRepository.getReservationsBetweenDates(start, end)

    /** 예약 추가 */
    fun addReservation(reservation: ReservationEntity, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.addReservation(reservation)
            onFinished()
        }
    }

    /** 예약 삭제 */
    fun deleteReservation(reservationId: Long, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            reservationRepository.deleteReservation(reservationId)
            onFinished()
        }
    }
}