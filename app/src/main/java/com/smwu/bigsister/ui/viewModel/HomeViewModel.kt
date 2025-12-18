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


    // 한국 시간대 로직 반영
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun saveReservation(reservation: ReservationEntity) {
        viewModelScope.launch {
            routineRepository.saveReservation(reservation)
        }
    }

    fun deleteReservation(reservationId: Long) {
        viewModelScope.launch {
            routineRepository.deleteReservation(reservationId)
        }
    }

    /** ✅ 핵심 수정: reservationRepository 대신 routineRepository를 통해 사용자 ID 필터링된 데이터 조회 */
    @OptIn(ExperimentalCoroutinesApi::class)
    val todaySchedules: StateFlow<List<ReservationEntity>> =
        selectedDate
            .flatMapLatest { date ->
                routineRepository.getReservationsByDate(date.toString())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    suspend fun calculateTotalDuration(routineId: Long): Long {
        return stepRepository.calculateTotalDurationOnce(routineId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getWeekDates(date: LocalDate): List<LocalDate> {
        val start = date.minusDays(date.dayOfWeek.ordinal.toLong())
        return List(7) { start.plusDays(it.toLong()) }
    }

    fun formatDuration(min: Long): String = "${min}분"
}