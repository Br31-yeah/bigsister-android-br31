package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    /** ✅ 날짜별 예약 목록 조회 */
    fun getReservationsByDate(date: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsByDate(date)
    }

    /** ✅ [에러 해결 1] 월별 예약 목록 조회
     * Repository의 함수명 getReservationsByMonth를 확인하여 호출합니다.
     */
    fun getReservationsByMonth(month: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsByMonth(month)
    }

    /** ✅ [에러 해결 2] 기간별 예약 조회
     * 매개변수 개수가 맞지 않던 문제를 해결했습니다.
     */
    fun getReservationsBetweenDates(startDate: String, endDate: String): Flow<List<ReservationEntity>> {
        return reservationRepository.getReservationsBetweenDates(startDate, endDate)
    }

    /** ✅ [에러 해결 3] 예약 추가 (서버 동기화 버전)
     * RoutineRepository에 구현된 saveReservation을 호출하여 Firestore와 동기화합니다.
     */
    fun addReservation(reservation: ReservationEntity) {
        viewModelScope.launch {
            routineRepository.saveReservation(reservation)
        }
    }

    /** ✅ [에러 해결 4] 예약 삭제 (서버 동기화 버전) */
    fun deleteReservation(reservationId: Long) {
        viewModelScope.launch {
            routineRepository.deleteReservation(reservationId)
        }
    }

    /** ✅ [에러 해결 5] 루틴 상세 및 스텝 조회
     * RoutineRepository의 함수를 사용하여 루틴과 스텝 정보를 가져옵니다.
     */
    fun loadRoutineDetail(routineId: Long) {
        viewModelScope.launch {
            val routineWithSteps = routineRepository.getRoutineWithSteps(routineId)
            // 필요한 경우 여기서 routineWithSteps.steps 등을 처리합니다.
        }
    }

    /** ✅ [에러 해결 6] 단일 예약 조회 */
    fun getReservationById(id: Long) {
        viewModelScope.launch {
            val reservation = reservationRepository.getReservationById(id)
            // 여기서 reservation.routineId 등을 사용할 수 있습니다.
        }
    }
}