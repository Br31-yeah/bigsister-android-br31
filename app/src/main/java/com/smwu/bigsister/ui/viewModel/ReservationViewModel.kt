package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    fun getReservationsByDate(date: String, userId: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsByDate(date, userId)
    }

    fun getReservationsByMonth(month: String, userId: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsByMonth(month, userId)
    }

    fun getReservationsBetweenDates(startDate: String, endDate: String, userId: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsBetweenDates(startDate, endDate, userId)
    }

    /** ✅ addReservation 함수 정의 (onSuccess 파라미터 포함) */
    fun addReservation(reservation: ReservationEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                reservationRepository.addReservation(reservation)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteReservation(reservationId: Long) {
        viewModelScope.launch {
            reservationRepository.deleteReservation(reservationId)
        }
    }

    fun getReservationById(id: Long) {
        viewModelScope.launch {
            reservationRepository.getReservationById(id)
        }
    }
}