package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.repository.CompletionRepository
import com.smwu.bigsister.data.repository.SettingsRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val stepRepository: StepRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(StatsUiState())
    val stats: StateFlow<StatsUiState> = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // 완료 기록(Completion)과 언니 모드 설정(SisterType)을 결합하여 관찰
            combine(
                completionRepository.getAllCompletions(),
                settingsRepository.sisterType
            ) { completions, sisterType ->
                calculateStats(completions, sisterType)
            }.collect { uiState ->
                _stats.value = uiState
            }
        }
    }

    private fun calculateStats(list: List<CompletionEntity>, character: String): StatsUiState {
        if (list.isEmpty()) {
            return StatsUiState(
                punctualityRate = 0,
                avgLateness = 0,
                streakDays = 0,
                weeklyData = generateEmptyWeeklyData(),
                sisterComment = when(character) {
                    "TSUNDERE" -> "흥, 기록도 없으면서 통계는 왜 봐? 얼른 움직여!"
                    "REALISTIC" -> "아직 기록이 없네? 오늘부터 언니랑 같이 시작해보자!"
                    "AI" -> "데이터가 존재하지 않습니다. 루틴 수행 후 다시 확인하십시오."
                    else -> "기록이 아직 없어요! 첫 루틴을 시작해보세요."
                }
            )
        }

        val punctualityRate = calcPunctualityRate(list)
        val avgLateness = calcAvgLateness(list)
        val streakDays = calcStreak(list)
        val weeklyData = calcWeeklyStats(list)

        // 성격 설정과 성취도를 바탕으로 코멘트 생성
        val comment = generateSisterMessage(character, punctualityRate)

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
        // 월요일(1) % 7 = 1 -> 인덱스 조정을 위해 요물림 처리 가능
        // Monday = 0 기준 (Calendar 로직에 맞춤)
        val index = date.dayOfWeek.value - 1
        return if (index < 0) 0 else index
    }

    private fun generateEmptyWeeklyData(): List<WeeklyStat> {
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return dayNames.map { WeeklyStat(it, 0, 0) }
    }

    /**
     * ✅ 언니 모드별 맞춤 멘트 생성 로직
     */
    private fun generateSisterMessage(character: String, punctuality: Int): String {
        return when (character) {
            "TSUNDERE" -> when {
                punctuality >= 80 -> "흥, 딱히 네가 잘해서 기분 좋은 건 아냐. (완벽해!)"
                punctuality >= 50 -> "뭐, 나쁘진 않네. 내일은 좀 더 일찍 움직여봐."
                else -> "너 진짜 이럴 거야? 당장 안 일어나면 국물도 없어!"
            }
            "REALISTIC" -> when {
                punctuality >= 80 -> "우리 동생 오늘 완전 갓생 살았네! 언니가 다 뿌듯해~"
                punctuality >= 50 -> "오늘 하루도 고생했어. 조금만 더 힘내볼까?"
                else -> "속상해라... 오늘은 많이 힘들었지? 내일은 언니랑 같이 다시 해보자."
            }
            "AI" -> when {
                punctuality >= 80 -> "데이터 분석 결과, 목표 달성률이 상위권입니다. 효율적입니다."
                punctuality >= 50 -> "준수한 성과입니다. 지속적인 수행 시 습관 형성이 가능합니다."
                else -> "경고: 루틴 이행률 저조. 시스템 최적화를 위해 즉시 수행이 필요합니다."
            }
            else -> "오늘도 언니랑 같이 힘내보자!"
        }
    }
}