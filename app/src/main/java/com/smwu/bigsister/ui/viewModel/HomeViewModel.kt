package com.smwu.bigsister.ui.viewModel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _userName = MutableStateFlow(Firebase.auth.currentUser?.displayName ?: "사용자")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now(ZoneId.of("Asia/Seoul")))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        refreshUserProfile()
    }

    private fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser
                if (user != null) {
                    user.reload().await()
                    _userName.value = user.displayName ?: "사용자"
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /**
     * ✅ 캐릭터 및 성취도별 멘트 로직
     */
    fun getSisterMessage(character: String, achievementRate: Int): String {
        return when (character) {
            "TSUNDERE" -> when {
                achievementRate >= 90 -> "흥, 딱히 네가 잘해서 기분 좋은 건 아냐. (완벽해!)"
                achievementRate >= 60 -> "뭐, 나쁘진 않네. 내일은 좀 더 일찍 움직여봐."
                else -> "너 진짜 이럴 거야? 당장 안 일어나면 국물도 없어!"
            }
            "REALISTIC" -> when {
                achievementRate >= 90 -> "우리 동생 오늘 완전 갓생 살았네! 언니가 다 뿌듯해~"
                achievementRate >= 60 -> "오늘 하루도 고생했어. 조금만 더 힘내볼까?"
                else -> "속상해라... 오늘은 많이 힘들었지? 내일은 언니랑 같이 다시 해보자."
            }
            "AI" -> when {
                achievementRate >= 90 -> "데이터 분석 결과, 목표 달성률이 최상위권입니다. 효율적입니다."
                achievementRate >= 60 -> "준수한 성과입니다. 지속적인 수행 시 습관 형성 가능성이 높습니다."
                else -> "경고: 루틴 이행률 저조. 시스템 최적화를 위해 즉시 수행이 필요합니다."
            }
            else -> "오늘도 언니랑 같이 힘내보자!"
        }
    }

    fun setSelectedDate(date: LocalDate) { _selectedDate.value = date }

    fun saveReservation(reservation: ReservationEntity) {
        viewModelScope.launch { routineRepository.saveReservation(reservation) }
    }

    fun deleteReservation(reservationId: Long) {
        viewModelScope.launch { routineRepository.deleteReservation(reservationId) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val todaySchedules: StateFlow<List<ReservationEntity>> =
        selectedDate.flatMapLatest { date -> routineRepository.getReservationsByDate(date.toString()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun calculateTotalDuration(routineId: Long): Long = stepRepository.calculateTotalDurationOnce(routineId)

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val start = date.minusDays(date.dayOfWeek.ordinal.toLong())
        return List(7) { start.plusDays(it.toLong()) }
    }
}