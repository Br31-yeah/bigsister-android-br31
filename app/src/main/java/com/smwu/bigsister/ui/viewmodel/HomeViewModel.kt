package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ✅ [추가] ReservationEntity import
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
// ✅ [수정] 'java.time' 대신 'java.text'와 'java.util' 사용
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeUiState(
    val selectedDate: String, // YYYY-MM-DD
    val scheduleList: List<ReservationRepository.ScheduledRoutineInfo> = emptyList(),
    val isLoading: Boolean = false
)

// ✅ [추가] 오늘 날짜를 "YYYY-MM-DD" 문자열로 반환하는 헬퍼 함수
private fun getTodayDateString(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    return formatter.format(Date())
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    // ✅ [수정] 초기값을 헬퍼 함수로 설정
    private val _uiState = MutableStateFlow(HomeUiState(selectedDate = getTodayDateString()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ✅ [수정] 'java.time' 대신 'java.text' 사용
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)

    init {
        // ✅ [수정] 'LocalDate' 대신 'Date' 객체 사용
        selectDate(Date())
    }

    /**
     * [수정됨] Date를 받아 String으로 변환 후 상태 업데이트
     */
    fun selectDate(date: Date) {
        val dateString = dateFormatter.format(date)
        _uiState.update { it.copy(selectedDate = dateString, isLoading = true) }
        loadSchedulesForDate(dateString)
    }

    private fun loadSchedulesForDate(date: String) {
        reservationRepository.getScheduledRoutinesForDate(date)
            .onEach { routines ->
                _uiState.update {
                    it.copy(
                        scheduleList = routines,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun deleteSchedule(reservationId: Int) {
        viewModelScope.launch {
            reservationRepository.deleteReservation(reservationId)
        }
    }

    /**
     * ✅ [추가] '루틴 추가' 화면(No. 4)에서 호출할 함수
     */
    fun addReservation(routineId: Int, date: String, startTime: String) {
        viewModelScope.launch {
            val newReservation = ReservationEntity(
                routineId = routineId,
                date = date,
                startTime = startTime
            )
            reservationRepository.addReservation(newReservation)
        }
    }
}