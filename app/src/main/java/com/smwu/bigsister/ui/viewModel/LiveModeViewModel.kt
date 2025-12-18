package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.CompletionEntity
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.model.VoiceType
import com.smwu.bigsister.data.repository.CompletionRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.SettingsRepository
import com.smwu.bigsister.data.repository.UserRepository
import com.smwu.bigsister.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

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
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TtsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveModeUiState())
    val uiState: StateFlow<LiveModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var allSteps: List<StepEntity> = emptyList()
    private var routineStartTime: Long = 0L
    private var plannedTotalDurationMillis: Long = 0L

    private var currentVoiceType: VoiceType = VoiceType.TSUNDERE
    private var isVoiceEnabled: Boolean = false

    // ✅ 에러 해결: NavType.LongType으로 전달된 경우 Long으로, 혹시 모를 Int 경우까지 대비해 안전하게 가져옵니다.
    private val routineId: Long = savedStateHandle.get<Long>("routineId")
        ?: savedStateHandle.get<Int>("routineId")?.toLong()
        ?: 0L

    init {
        // 설정 로드
        viewModelScope.launch {
            isVoiceEnabled = settingsRepository.voiceAlarm.first()
            val typeString = settingsRepository.sisterType.first()
            currentVoiceType = when (typeString) {
                "REALISTIC" -> VoiceType.REALISTIC
                "AI" -> VoiceType.AI
                else -> VoiceType.TSUNDERE
            }
        }

        // 루틴 로드 시작
        if (routineId != 0L) {
            loadRoutine(routineId)
        } else {
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    private fun loadRoutine(id: Long) {
        routineStartTime = System.currentTimeMillis()
        viewModelScope.launch {
            // routineRepository의 getRoutineWithSteps 호출
            val routineWithSteps = routineRepository.getRoutineWithSteps(id) ?: run {
                _uiState.update { it.copy(isLoading = false, isFinished = true) }
                return@launch
            }

            allSteps = routineWithSteps.steps
            plannedTotalDurationMillis = allSteps.sumOf { (it.calculatedDuration ?: it.baseDuration) * 60_000L }

            _uiState.update {
                it.copy(
                    routineTitle = routineWithSteps.routine.title,
                    totalSteps = allSteps.size,
                    isLoading = false
                )
            }

            speakSister(SisterEvent.START)
            startStep(0)
        }
    }

    private fun startStep(stepIndex: Int) {
        if (stepIndex >= allSteps.size) {
            finishRoutine()
            return
        }
        val step = allSteps[stepIndex]
        val durationMillis = (step.calculatedDuration ?: step.baseDuration) * 60_000L
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
        var announcedOneMinute = false
        var announcedFinal = false

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                remaining -= 1000
                if (remaining >= 0) {
                    _uiState.update { it.copy(remainingTimeInMillis = remaining) }
                    val remainingSeconds = remaining / 1000
                    if (remainingSeconds == 60L && !announcedOneMinute) {
                        speakTimeRemaining("1분")
                        announcedOneMinute = true
                    }
                    if (remainingSeconds == 10L && !announcedFinal) {
                        speakTimeRemaining("10초")
                        announcedFinal = true
                    }
                } else {
                    val overtime = -remaining
                    _uiState.update { it.copy(remainingTimeInMillis = 0, isOvertime = true, overtimeInMillis = overtime) }
                    // 10초마다 독설/알람
                    if (overtime > 0 && overtime % 10000L == 0L) speakSister(SisterEvent.LATE)
                }
            }
        }
    }

    fun completeStep() {
        timerJob?.cancel()
        if (_uiState.value.isOvertime) speakSister(SisterEvent.COMPLETE_LATE)
        else speakSister(SisterEvent.COMPLETE_ON_TIME)
        startStep(_uiState.value.currentStepIndex + 1)
    }

    fun skipStep() {
        timerJob?.cancel()
        if (_uiState.value.isOvertime) speakSister(SisterEvent.SKIP_LATE)
        else speakSister(SisterEvent.SKIP_ON_TIME)
        startStep(_uiState.value.currentStepIndex + 1)
    }

    private fun finishRoutine() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true) }
        speakSister(SisterEvent.FINISH)

        viewModelScope.launch {
            val completionTime = System.currentTimeMillis()
            val totalTimeMillis = completionTime - routineStartTime
            val currentUserId = userRepository.firebaseUser.value?.uid ?: ""

            completionRepository.insertCompletion(
                CompletionEntity(
                    routineId = routineId,
                    userId = currentUserId,
                    date = LocalDate.now().toString(),
                    completedAt = completionTime,
                    totalTime = totalTimeMillis / 1000,
                    wasLate = totalTimeMillis > plannedTotalDurationMillis
                )
            )
        }
    }

    private fun speakTimeRemaining(timeStr: String) {
        if (!isVoiceEnabled) return
        val message = when (currentVoiceType) {
            VoiceType.TSUNDERE -> "야, 너 이제 ${timeStr}밖에 안 남았어. 빨리 안 해?"
            VoiceType.REALISTIC -> "이제 ${timeStr} 남았다. 슬슬 마무리하고 다음 거 준비해."
            VoiceType.AI -> "알림. 종료까지 ${timeStr} 남았습니다. 공정 속도를 유지하십시오."
        }
        ttsManager.speak(message, currentVoiceType)
    }

    private fun speakSister(event: SisterEvent) {
        if (!isVoiceEnabled) return
        val message = when (currentVoiceType) {
            VoiceType.TSUNDERE -> when (event) {
                SisterEvent.START -> "시작해. 늦장 부리면 두고 갈 거야."
                SisterEvent.LATE -> "시간 초과야. 계획 망가지는 거 딱 질색인데."
                SisterEvent.FINISH -> "흥, 다 끝냈네. 뭐, 수고했어."
                SisterEvent.COMPLETE_ON_TIME -> "제시간에 했네? 나쁘지 않아."
                SisterEvent.COMPLETE_LATE -> "겨우 끝냈네. 다음 건 좀 더 서둘러."
                SisterEvent.SKIP_ON_TIME -> "벌써 넘기는 거야? 빠르긴 하네."
                SisterEvent.SKIP_LATE -> "시간 없어서 넘긴 거지? 다음 단계는 제대로 해."
            }
            VoiceType.REALISTIC -> when (event) {
                SisterEvent.START -> "지금 시작 안 하면 지각이야. 당장 움직여."
                SisterEvent.LATE -> "시간 넘었어. 정신 안 차려? 빨리 해."
                SisterEvent.FINISH -> "좋아, 오늘은 여기까지. 내일도 이렇게만 해."
                SisterEvent.COMPLETE_ON_TIME -> "잘했어. 시간 딱 맞췄네. 계속 그렇게 해."
                SisterEvent.COMPLETE_LATE -> "늦었잖아. 다음부턴 더 빨리 움직여."
                SisterEvent.SKIP_ON_TIME -> "일단 넘어가는구나. 행동이 빨라서 좋네."
                SisterEvent.SKIP_LATE -> "시간 다 돼서 넘기는 거야? 다음 단계는 죽기 살기로 해."
            }
            VoiceType.AI -> when (event) {
                SisterEvent.START -> "루틴 프로세스를 시작합니다. 효율을 유지하십시오."
                SisterEvent.LATE -> "경고. 예정된 시간을 초과했습니다. 속도를 높이세요."
                SisterEvent.FINISH -> "루틴 종료. 수행 능력이 매우 논리적이었습니다."
                SisterEvent.COMPLETE_ON_TIME -> "단계 완료 확인. 소요 시간 적절함."
                SisterEvent.COMPLETE_LATE -> "단계 완료되었으나 시간 지연 발생. 가속이 필요합니다."
                SisterEvent.SKIP_ON_TIME -> "단계 건너뛰기 실행. 진행 속도가 향상되었습니다."
                SisterEvent.SKIP_LATE -> "시간 초과로 인한 건너뛰기. 다음 단계는 가속하십시오."
            }
        }
        ttsManager.speak(message, currentVoiceType)
    }

    override fun onCleared() {
        timerJob?.cancel()
        ttsManager.stop()
        super.onCleared()
    }
}

enum class SisterEvent {
    START, LATE, FINISH, COMPLETE_ON_TIME, COMPLETE_LATE, SKIP_ON_TIME, SKIP_LATE
}