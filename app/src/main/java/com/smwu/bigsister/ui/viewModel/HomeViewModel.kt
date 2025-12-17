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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val reservationRepository: ReservationRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    /* ────────────────────────────────
       📅 선택된 날짜
    ──────────────────────────────── */

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /* ────────────────────────────────
       📋 오늘 예약된 루틴 목록 (핵심)
       ✔ 같은 루틴
       ✔ 다른 시작 시간
       ✔ 전부 표시됨
    ──────────────────────────────── */

    val todaySchedules: StateFlow<List<ReservationEntity>> =
        selectedDate
            .flatMapLatest { date ->
                reservationRepository.getReservationsByDate(date.toString())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /* ────────────────────────────────
       📦 루틴 목록
    ──────────────────────────────── */

    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /* ────────────────────────────────
       ⏱ 루틴 총 소요 시간
    ──────────────────────────────── */

    suspend fun calculateTotalDuration(routineId: Long): Long {
        return stepRepository.calculateTotalDurationOnce(routineId)
    }

    /* ────────────────────────────────
       UI 유틸
    ──────────────────────────────── */

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val start = date.minusDays(date.dayOfWeek.ordinal.toLong())
        return List(7) { start.plusDays(it.toLong()) }
    }

    fun formatDuration(min: Long): String = "${min}분"
}