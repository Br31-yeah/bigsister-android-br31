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
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val ttsManager: TtsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveModeUiState())
    val uiState: StateFlow<LiveModeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var allSteps: List<StepEntity> = emptyList()

    private var routineId: Long = 0L
    private var routineStartTime: Long = 0L
    private var plannedTotalDurationMillis: Long = 0L

    private var currentVoiceType: VoiceType = VoiceType.TSUNDERE
    private var isVoiceEnabled: Boolean = false

    init {
        // ⚙️ 사용자의 음성 캐릭터 설정 및 알람 On/Off 설정 로드
        viewModelScope.launch {
            isVoiceEnabled = settingsRepository.voiceAlarm.first()
            val typeString = settingsRepository.sisterType.first()
            currentVoiceType = when (typeString) {
                "REALISTIC" -> VoiceType.REALISTIC
                "AI" -> VoiceType.AI
                else -> VoiceType.TSUNDERE
            }
        }

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

            // ⏱️ 조원의 필드명(baseDuration) 및 단위(분) 유지: 밀리초로 변환
            plannedTotalDurationMillis = allSteps.sumOf {
                (it.calculatedDuration ?: it.baseDuration) * 60_000L
            }

            _uiState.update {
                it.copy(
                    routineTitle = routineWithSteps.routine.title,
                    totalSteps = allSteps.size,
                    isLoading = false
                )
            }

            speakSister(SisterEvent.START) // "시작해!"
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
        // ⏱️ 실시간 계산된 시간이 있으면 우선 적용, 없으면 기준 시간 사용 (분 -> 밀리초)
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
                    val overtime = -remaining
                    _uiState.update {
                        it.copy(
                            remainingTimeInMillis = 0,
                            isOvertime = true,
                            overtimeInMillis = overtime
                        )
                    }

                    // 🔔 지연 시 10초마다 캐릭터별 독촉 TTS 실행
                    if (overtime > 0 && overtime % 10000L == 0L) {
                        speakSister(SisterEvent.LATE)
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

        // ✅ 상황별 TTS 분기: 제시간 완료 vs 지연 완료
        if (_uiState.value.isOvertime) {
            speakSister(SisterEvent.COMPLETE_LATE)
        } else {
            speakSister(SisterEvent.COMPLETE_ON_TIME)
        }

        startStep(_uiState.value.currentStepIndex + 1)
    }

    fun skipStep() {
        timerJob?.cancel()

        // ✅ 상황별 TTS 분기: 제시간 건너뛰기 vs 지연 중 건너뛰기
        if (_uiState.value.isOvertime) {
            speakSister(SisterEvent.SKIP_LATE)
        } else {
            speakSister(SisterEvent.SKIP_ON_TIME)
        }

        startStep(_uiState.value.currentStepIndex + 1)
    }

    /* ────────────────────────────────
       FINISH
    ──────────────────────────────── */

    private fun finishRoutine() {
        timerJob?.cancel()
        _uiState.update { it.copy(isFinished = true) }

        speakSister(SisterEvent.FINISH) // "수고했어"

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
                    totalTime = totalTimeMillis / 1000, // Long (초 단위 저장)
                    wasLate = totalTimeMillis > plannedTotalDurationMillis
                )
            )
        }
    }

    // 🎙️ 설정된 캐릭터별 맞춤 대사 실행 로직
    private fun speakSister(event: SisterEvent) {
        if (!isVoiceEnabled) return

        val message = when (currentVoiceType) {
            // 1. 츤데레 (새침하고 퉁명스럽지만 사실은 걱정해주는 말투)
            VoiceType.TSUNDERE -> when (event) {
                SisterEvent.START -> "시작해. 늦장 부리다 늦어도 난 절대 안 도와줄 거니까, 알았어?"
                SisterEvent.LATE -> "너 지금 뭐 하는 거야? 벌써 시간 넘었잖아! 빨리빨리 좀 움직여!"
                SisterEvent.FINISH -> "흥, 다 끝냈네. 뭐... 이번엔 좀 봐줄 만했어. 수고했어."
                SisterEvent.COMPLETE_ON_TIME -> "제시간에 끝냈네? 딱히 칭찬하는 건 아니니까 착각하지 마."
                SisterEvent.COMPLETE_LATE -> "겨우 끝낸 거야? 거 봐, 내가 서두르라고 했지! 다음부턴 똑바로 해."
                SisterEvent.SKIP_ON_TIME -> "벌써 넘기겠다고? 성격 급하긴... 뭐, 빨리 다음 단계나 해."
                SisterEvent.SKIP_LATE -> "시간 없어서 건너뛰는 거지? 칠칠치 못하게... 다음 단계는 제대로 하라고!"
            }

            // 2. 현실 언니 (단호하고 뼈 때리지만 쿨하게 챙겨주는 말투)
            VoiceType.REALISTIC -> when (event) {
                SisterEvent.START -> "야, 지금 시작 안 하면 너 100% 지각이다. 당장 움직여."
                SisterEvent.LATE -> "너 아직도 그거 붙잡고 있어? 시간 다 됐어. 정신 차리고 빨리 하자."
                SisterEvent.FINISH -> "드디어 끝났네. 고생했다! 우리 내일은 좀 더 일찍 일어나 볼까?"
                SisterEvent.COMPLETE_ON_TIME -> "웬일이야? 시간 딱 맞췄네. 아주 칭찬해. 계속 이렇게만 가자."
                SisterEvent.COMPLETE_LATE -> "늦었잖아. 언니가 아까 빨리 하라고 했지? 다음 건 좀 더 빨리 움직여."
                SisterEvent.SKIP_ON_TIME -> "어, 넘어가게? 행동 빠릿빠릿해서 좋네. 쿨하게 다음 거 가보자."
                SisterEvent.SKIP_LATE -> "시간 모자라서 패스하는 거지? 어쩔 수 없지 뭐. 다음 단계는 죽기 살기로 해라."
            }

            // 3. AI (기계적이고 분석적이며 신뢰감을 주는 말투)
            VoiceType.AI -> when (event) {
                SisterEvent.START -> "루틴 프로세스를 활성화합니다. 최적의 효율을 위해 즉시 시작하십시오."
                SisterEvent.LATE -> "경고. 예정된 목표 시간을 초과했습니다. 신속하게 완료할 것을 권장합니다."
                SisterEvent.FINISH -> "전체 루틴이 종료되었습니다. 오늘의 수행 데이터는 매우 긍정적입니다."
                SisterEvent.COMPLETE_ON_TIME -> "단계 완료 확인. 목표 시간 이내에 수행되었습니다. 다음 단계를 준비하십시오."
                SisterEvent.COMPLETE_LATE -> "단계 완료 확인. 목표 시간보다 지연되었습니다. 가속 모드가 필요합니다."
                SisterEvent.SKIP_ON_TIME -> "단계 건너뛰기 실행. 전체 공정 속도가 향상되었습니다."
                SisterEvent.SKIP_LATE -> "시간 초과로 인한 건너뛰기가 발생했습니다. 다음 공정의 효율을 최대치로 높이십시오."
            }
        }
        ttsManager.speak(message, currentVoiceType)
    }

    override fun onCleared() {
        timerJob?.cancel()
        ttsManager.stop() // ViewModel 종료 시 음성 중단
        super.onCleared()
    }
}

/**
 * TTS가 발생하는 상황 정의
 */
enum class SisterEvent {
    START, LATE, FINISH,
    COMPLETE_ON_TIME, COMPLETE_LATE, SKIP_ON_TIME, SKIP_LATE
}