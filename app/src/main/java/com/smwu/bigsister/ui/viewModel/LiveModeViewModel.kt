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
        val navRoutineId = savedStateHandle.get<Int>("routineId")?.toLong()
        if (navRoutineId != null) {
            loadRoutine(navRoutineId)
        } else {
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    /**
     * 루틴 로딩
     */
    private fun loadRoutine(id: Long) {
        routineId = id
        routineStartTime = System.currentTimeMillis()

        viewModelScope.launch {
            val routineWithSteps = routineRepository.getRoutineWithSteps(id)

            if (routineWithSteps == null || routineWithSteps.steps.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isFinished = true) }
                return@launch
            }

            allSteps = routineWithSteps.steps
            plannedTotalDuration = allSteps.sumOf { it.duration * 60 * 1000L }

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

    /**
     * 특정 Step 시작
     */
    private fun startStep(stepIndex: Int) {
        if (stepIndex >= allSteps.size) {
            finishRoutine()
            return
        }

        val step = allSteps[stepIndex]
        val durationMillis = (step.duration * 60 * 1000L)

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

    /**
     * 타이머 시작
     */
    private fun startTimer(durationInMillis: Long) {
        timerJob?.cancel()
        var remaining = durationInMillis

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                remaining -= 1000

                if (remaining >= 0) {
                    _uiState.update { it.copy(remainingTimeInMillis = remaining) }
                } else {
                    val overtime = -remaining

                    _uiState.update {
                        it.copy(
                            remainingTimeInMillis = 0,
                            isOvertime = true,
                            overtimeInMillis = overtime
                        )
                    }

                    if (overtime % 10000L == 0L) {
                        // ttsManager.speak("아직도 안 끝났어?")
                    }
                }
            }
        }
    }

    /**
     * 완료 버튼
     */
    fun completeStep() {
        timerJob?.cancel()
        val next = _uiState.value.currentStepIndex + 1

        if (next < _uiState.value.totalSteps) {
            startStep(next)
        } else {
            finishRoutine()
        }
    }

    /**
     * 건너뛰기
     */
    fun skipStep() {
        completeStep()
    }

    /**
     * 루틴 종료
     */
    private fun finishRoutine() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true) }

        viewModelScope.launch {
            val completionTime = System.currentTimeMillis()
            val totalTimeMillis = completionTime - routineStartTime
            val wasLate = totalTimeMillis > plannedTotalDuration

            val completionRecord = CompletionEntity(
                routineId = routineId,
                date = LocalDate.now().toString(),
                completedAt = completionTime,
                totalTime = (totalTimeMillis / 1000).toInt(),
                wasLate = wasLate
            )

            completionRepository.insertCompletion(completionRecord)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}