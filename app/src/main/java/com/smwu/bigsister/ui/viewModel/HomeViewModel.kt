package com.smwu.bigsister.ui.viewModel

import StepRepository
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val reservationRepository: ReservationRepository,
    private val stepRepository: StepRepository   // ← duration 계산용
) : ViewModel() {

    // ──────────────────────────────────────
    // 날짜 상태
    // ──────────────────────────────────────
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        loadSchedules()
    }

    // ──────────────────────────────────────
    // 루틴 목록
    // ──────────────────────────────────────
    private val _routines = MutableStateFlow<List<RoutineEntity>>(emptyList())
    val routines: StateFlow<List<RoutineEntity>> = _routines.asStateFlow()

    // ──────────────────────────────────────
    // 전체 스케줄
    // ──────────────────────────────────────
    private val _schedules = MutableStateFlow<List<ReservationEntity>>(emptyList())
    val schedules: StateFlow<List<ReservationEntity>> = _schedules.asStateFlow()

    // 선택된 날짜의 스케줄
    val todaySchedules: StateFlow<List<ReservationEntity>> =
        combine(_schedules, _selectedDate) { list, date ->
            val d = date.toString()
            list.filter { it.date == d }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadRoutines()
        loadSchedules()
    }

    // ──────────────────────────────────────
    // DB 로딩
    // ──────────────────────────────────────
    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.getAllRoutines().collect {
                _routines.value = it
            }
        }
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.toString()
            reservationRepository.getReservationsByDate(dateStr).collect {
                _schedules.value = it
            }
        }
    }

    // ──────────────────────────────────────
    // Step에서 duration 합산 계산
    // ──────────────────────────────────────
    suspend fun calculateTotalDuration(routineId: Long): Int {
        return stepRepository.calculateTotalDuration(routineId)
    }

    // ──────────────────────────────────────
    // UI 유틸
    // ──────────────────────────────────────
    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val start = date.minusDays(date.dayOfWeek.ordinal.toLong())
        return List(7) { start.plusDays(it.toLong()) }
    }

    fun getSchedulesForDate(date: LocalDate): List<ReservationEntity> {
        return _schedules.value.filter { it.date == date.toString() }
    }

    fun toggleCalendar() {
        // TODO: UI 확장 기능
    }

    fun showAddRoutine(date: LocalDate) {
        // TODO: 네비게이션 이동
    }

    fun formatDuration(min: Int): String = "${min}분"
}