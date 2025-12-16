package com.smwu.bigsister.ui.viewModel

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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val reservationRepository: ReservationRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    /* ───────── 날짜 ───────── */

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        loadSchedules()
    }

    /* ───────── 루틴 ───────── */

    private val _routines = MutableStateFlow<List<RoutineEntity>>(emptyList())
    val routines: StateFlow<List<RoutineEntity>> = _routines.asStateFlow()

    /* ───────── 예약 ───────── */

    private val _schedules = MutableStateFlow<List<ReservationEntity>>(emptyList())
    val schedules: StateFlow<List<ReservationEntity>> = _schedules.asStateFlow()

    val todaySchedules: StateFlow<List<ReservationEntity>> =
        combine(_schedules, _selectedDate) { list, date ->
            list.filter { it.date == date.toString() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadRoutines()
        loadSchedules()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            routineRepository.getAllRoutines().collect {
                _routines.value = it
            }
        }
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            reservationRepository
                .getReservationsByDate(_selectedDate.value.toString())
                .collect { _schedules.value = it }
        }
    }

    /* ───────── duration 계산 (✅ Long) ───────── */

    suspend fun calculateTotalDuration(routineId: Long): Long {
        return stepRepository.calculateTotalDurationOnce(routineId)
    }

    /* ───────── UI 유틸 ───────── */

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val start = date.minusDays(date.dayOfWeek.ordinal.toLong())
        return List(7) { start.plusDays(it.toLong()) }
    }

    fun formatDuration(min: Long): String = "${min}분"
}