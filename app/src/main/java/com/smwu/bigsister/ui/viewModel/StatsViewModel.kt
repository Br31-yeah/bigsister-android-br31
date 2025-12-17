package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.repository.CompletionRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class WeeklyStat(
    val day: String,
    val onTime: Int,
    val late: Int
)

data class StatsUiState(
    val punctualityRate: Int = 0,
    val avgLateness: Int = 0,
    val streakDays: Int = 0,
    val weeklyData: List<WeeklyStat> = emptyList(),
    val sisterComment: String = ""
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val completionRepository: CompletionRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(StatsUiState())
    val stats: StateFlow<StatsUiState> = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            completionRepository.getAllCompletions()
                .collect { list ->
                    _stats.value = calculateStats(list)
                }
        }
    }

    private suspend fun calculateStats(list: List<CompletionEntity>): StatsUiState {
        if (list.isEmpty()) {
            return StatsUiState(
                punctualityRate = 0,
                avgLateness = 0,
                streakDays = 0,
                weeklyData = generateEmptyWeeklyData(),
                sisterComment = "기록이 아직 없어요!"
            )
        }

        val punctualityRate = calcPunctualityRate(list)
        val avgLateness = calcAvgLateness(list)
        val streakDays = calcStreak(list)
        val weeklyData = calcWeeklyStats(list)
        val comment = generateComment(punctualityRate)

        return StatsUiState(
            punctualityRate = punctualityRate,
            avgLateness = avgLateness,
            streakDays = streakDays,
            weeklyData = weeklyData,
            sisterComment = comment
        )
    }

    //-----------------------------------------
    // 📌 통계 계산 함수들
    //-----------------------------------------

    private fun calcPunctualityRate(list: List<CompletionEntity>): Int {
        val onTime = list.count { !it.wasLate }
        return ((onTime.toDouble() / list.size) * 100).toInt()
    }

    private fun calcAvgLateness(list: List<CompletionEntity>): Int {
        val lateList = list.filter { it.wasLate }
        if (lateList.isEmpty()) return 0
        return (lateList.map { it.totalTime }.average() / 60).toInt() // 분 단위
    }

    private fun calcStreak(list: List<CompletionEntity>): Int {
        val sorted = list.sortedByDescending { it.completedAt }
        var streak = 0

        for (c in sorted) {
            if (!c.wasLate) streak++
            else break
        }
        return streak
    }

    private fun calcWeeklyStats(list: List<CompletionEntity>): List<WeeklyStat> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val result = MutableList(7) { WeeklyStat(dayNames[it], 0, 0) }

        list.forEach { c ->
            val dayIndex = convertTimestampToDayIndex(c.completedAt)
            val wasLate = c.wasLate

            val current = result[dayIndex]
            result[dayIndex] = if (wasLate) {
                current.copy(late = current.late + 1)
            } else {
                current.copy(onTime = current.onTime + 1)
            }
        }
        return result
    }

    private fun convertTimestampToDayIndex(timestamp: Long): Int {
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Monday = 0, Sunday = 6
        return (date.dayOfWeek.value % 7)
    }

    private fun generateEmptyWeeklyData(): List<WeeklyStat> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return dayNames.map { WeeklyStat(it, 0, 0) }
    }

    private fun generateComment(punctuality: Int): String =
        when {
            punctuality >= 80 -> "잘하고 있어! 지금 페이스 유지하자!"
            punctuality >= 50 -> "조금만 더 노력하면 훨씬 좋아질 거야."
            else -> "괜찮아, 천천히 습관 들여보자. 언니가 응원해!"
        }
}