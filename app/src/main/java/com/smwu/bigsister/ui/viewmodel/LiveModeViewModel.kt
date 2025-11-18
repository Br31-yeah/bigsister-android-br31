package com.smwu.bigsister.ui.viewmodel

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
import javax.inject.Inject

/**
 * '루틴 실행 화면'의 UI 상태
 */
data class LiveModeUiState(
    val routineTitle: String = "",
    val currentStepIndex: Int = 0, // 0-indexed
    val totalSteps: Int = 0,
    val currentStep: StepEntity? = null,
    val remainingTimeInMillis: Long = 0, // 현재 단계의 남은 시간
    val isOvertime: Boolean = false, // 시간 초과 여부
    val overtimeInMillis: Long = 0, // 초과 시간
    val isFinished: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LiveModeViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val completionRepository: CompletionRepository,
    // private val ttsManager: TTSManager, // TODO: TTS 모듈 추가 시
    savedStateHandle: SavedStateHandle // NavGraph에서 routineId를 받기 위해 필요
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveModeUiState())
    val uiState: StateFlow<LiveModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var allSteps: List<StepEntity> = emptyList()
    private var routineId: Int = 0
    private var routineStartTime: Long = 0 // 루틴 전체 시작 시간
    private var plannedTotalDuration: Long = 0 // 루틴의 총 계획 시간 (millis)

    init {
        // NavGraph에서 전달받은 "routineId"
        val navRoutineId = savedStateHandle.get<Int>("routineId")
        if (navRoutineId != null) {
            loadRoutine(navRoutineId)
        } else {
            _uiState.update { it.copy(isLoading = false, isFinished = true) } // ID 없으면 종료
        }
    }

    /**
     * ID로 루틴과 단계를 불러와 타이머를 시작합니다.
     */
    private fun loadRoutine(id: Int) {
        this.routineId = id
        this.routineStartTime = System.currentTimeMillis() // 루틴 시작 시간 기록

        viewModelScope.launch {
            val routineWithSteps = routineRepository.getRoutineWithSteps(id)
            if (routineWithSteps == null || routineWithSteps.steps.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, isFinished = true) } // 루틴/단계 없으면 종료
                return@launch
            }

            allSteps = routineWithSteps.steps
            plannedTotalDuration = allSteps.sumOf { it.duration } * 60 * 1000L

            _uiState.update {
                it.copy(
                    routineTitle = routineWithSteps.routine.title,
                    totalSteps = allSteps.size,
                    isLoading = false
                )
            }
            startStep(0) // 0번 인덱스부터 단계 시작
        }
    }

    /**
     * 특정 인덱스의 단계를 시작합니다.
     */
    private fun startStep(stepIndex: Int) {
        if (stepIndex >= allSteps.size) {
            finishRoutine() // 모든 단계 완료
            return
        }

        val step = allSteps[stepIndex]
        val durationMillis = (step.duration * 60 * 1000).toLong()

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
     * 실시간 카운트다운 타이머
     */
    private fun startTimer(durationInMillis: Long) {
        timerJob?.cancel() // 이전 타이머가 있다면 취소
        var remaining = durationInMillis

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1초마다 갱신
                remaining -= 1000

                if (remaining >= 0) {
                    _uiState.update { it.copy(remainingTimeInMillis = remaining) }
                } else {
                    // 시간 초과! (Overtime)
                    val overtime = -remaining
                    _uiState.update {
                        it.copy(
                            remainingTimeInMillis = 0,
                            isOvertime = true,
                            overtimeInMillis = overtime
                        )
                    }
                    // ✅ [수정] Line 139 오류 해결: Int(10000, 0) -> Long(10000L, 0L)
                    if (overtime % 10000L == 0L) { // 10초마다
                        // ttsManager.speak("아직도 안 끝났어? 대단하네.")
                    }
                }
            }
        }
    }

    /**
     * '완료' 버튼 클릭
     */
    fun completeStep() {
        timerJob?.cancel()
        val nextStepIndex = _uiState.value.currentStepIndex + 1
        if (nextStepIndex < _uiState.value.totalSteps) {
            startStep(nextStepIndex)
        } else {
            finishRoutine()
        }
    }

    /**
     * '건너뛰기' 버튼 클릭
     */
    fun skipStep() {
        // TODO: 건너뛰기 로직 (통계에 반영 등)
        completeStep() // 일단 완료와 동일하게 처리
    }

    /**
     * 루틴 전체 완료
     */
    private fun finishRoutine() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true) }

        // 완료 기록을 DB에 저장
        viewModelScope.launch {
            val completionTime = System.currentTimeMillis()
            val totalTimeMillis = completionTime - routineStartTime
            val wasLate = totalTimeMillis > plannedTotalDuration

            val completionRecord = CompletionEntity(
                routineId = routineId, // ✅ [수정] Line 182 오류 해결: 'this.' 삭제
                completedAt = completionTime,
                totalTime = (totalTimeMillis / 1000).toInt(), // 초 단위로 저장
                wasLate = wasLate
            )
            completionRepository.addCompletion(completionRecord)
        }
    }

    override fun onCleared() {
        timerJob?.cancel() // ViewModel 파괴 시 타이머 정리
        super.onCleared()
    }
}