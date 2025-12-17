package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.repository.CompletionRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.smwu.bigsister.data.repository.UserRepository

/**
 * '루틴 실행 화면'을 위한 UI 상태
 */
data class LiveModeUiState(
    val routineTitle: String = "",
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val currentStep: StepEntity? = null,
    val remainingTimeInMillis: Long = 0,
    val isOvertime: Boolean = false,
    val overtimeInMillis: Long = 0,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LiveModeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val completionRepository: CompletionRepository,
    private val userRepository: UserRepository, // ✅ userId 획득용
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveModeUiState())
    val uiState: StateFlow<LiveModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var allSteps: List<StepEntity> = emptyList()

    private var routineId: Long = 0L
    private var routineStartTime: Long = 0L
    private var plannedTotalDuration: Long = 0L

    init {
        savedStateHandle.get<Int>("routineId")?.toLong()?.let {
            loadRoutine(it)
        } ?: run {
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    /* ────────────────────────────────
       ROUTINE LOAD
    ──────────────────────────────── */

    private fun loadRoutine(id: Long) {
        routineId = id
        routineStartTime = System.currentTimeMillis()

        viewModelScope.launch {
            val routineWithSteps =
                routineRepository.getRoutineWithSteps(id)
                    ?: run {
                        _uiState.update {
                            it.copy(isLoading = false, isFinished = true)
                        }
                        return@launch
                    }

            allSteps = routineWithSteps.steps
            plannedTotalDuration =
                allSteps.sumOf { it.calculatedDuration ?: it.baseDuration }

            _uiState.update {
                it.copy(
                    routineTitle = routineWithSteps.routine.title,
                    totalSteps = allSteps.size,
                    isLoading = false
                )
            }

            startStep(0)
        }
    }

    /* ────────────────────────────────
       STEP CONTROL
    ──────────────────────────────── */

    private fun startStep(stepIndex: Int) {
        if (stepIndex >= allSteps.size) {
            finishRoutine()
            return
        }

        val step = allSteps[stepIndex]
        val durationMinutes = step.calculatedDuration ?: step.baseDuration
        val durationMillis = durationMinutes * 60_000L

        _uiState.update {
            it.copy(
                currentStepIndex = stepIndex,
                currentStep = step,
                remainingTimeInMillis = durationMillis,
                isOvertime = false,
                overtimeInMillis = 0
            )
        }

        startTimer(durationMillis)
    }

    private fun startTimer(durationInMillis: Long) {
        timerJob?.cancel()
        var remaining = durationInMillis

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                remaining -= 1000

                if (remaining >= 0) {
                    _uiState.update {
                        it.copy(remainingTimeInMillis = remaining)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            remainingTimeInMillis = 0,
                            isOvertime = true,
                            overtimeInMillis = -remaining
                        )
                    }
                }
            }
        }
    }

    /* ────────────────────────────────
       UI ACTIONS
    ──────────────────────────────── */

    fun completeStep() {
        timerJob?.cancel()
        startStep(_uiState.value.currentStepIndex + 1)
    }

    fun skipStep() {
        completeStep()
    }

    /* ────────────────────────────────
       FINISH (🔥 userId 반영 포인트)
    ──────────────────────────────── */

    private fun finishRoutine() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true) }

        viewModelScope.launch {
            val completionTime = System.currentTimeMillis()
            val totalTimeMillis = completionTime - routineStartTime

            val currentUserId =
                userRepository.firebaseUser.value?.uid ?: ""

            completionRepository.insertCompletion(
                CompletionEntity(
                    routineId = routineId,
                    userId = currentUserId,
                    date = LocalDate.now().toString(),
                    completedAt = completionTime,
                    totalTime = totalTimeMillis / 1000, // Long
                    wasLate = totalTimeMillis > plannedTotalDuration
                )
            )
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}