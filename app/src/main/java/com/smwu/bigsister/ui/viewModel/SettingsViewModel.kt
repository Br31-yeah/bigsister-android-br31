package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.model.VoiceType
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.SettingsRepository
import com.smwu.bigsister.data.repository.UserRepository
import com.smwu.bigsister.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository,
    private val ttsManager: TtsManager
) : ViewModel() {

    // 로컬 데이터 구독
    val sisterType = settingsRepository.sisterType.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TSUNDERE")
    val pushAlarm = settingsRepository.pushAlarm.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val voiceAlarm = settingsRepository.voiceAlarm.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val intensity = settingsRepository.intensity.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "보통 - 표준 알림")
    val timing = settingsRepository.timing.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "마감 3분 전")

    // ✅ 온보딩 상태 구독
    val hasSeenOnboarding = settingsRepository.hasSeenOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // 앱 시작 시 혹은 로그인 후 서버 데이터와 동기화
        viewModelScope.launch { settingsRepository.syncFromFirebase() }
    }

    // ✅ 서버와 로컬 동시 업데이트 함수
    private fun updateSettings() {
        viewModelScope.launch {
            val currentSettings = mapOf(
                "sisterType" to sisterType.value,
                "pushAlarm" to pushAlarm.value,
                "voiceAlarm" to voiceAlarm.value,
                "intensity" to intensity.value,
                "timing" to timing.value
            )
            settingsRepository.uploadSettingsToFirebase(currentSettings)
        }
    }

    // ✅ 온보딩 완료 처리
    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.saveOnboardingSeen(true)
        }
    }

    fun setSisterType(type: String) {
        viewModelScope.launch {
            settingsRepository.saveSisterType(type)
            updateSettings()
        }
    }

    fun setPushAlarm(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.savePushAlarm(isEnabled)
            updateSettings()
        }
    }

    fun setVoiceAlarm(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveVoiceAlarm(isEnabled)
            updateSettings()
        }
    }

    fun setIntensity(value: String) {
        viewModelScope.launch {
            settingsRepository.saveIntensity(value)
            updateSettings()
        }
    }

    fun setTiming(value: String) {
        viewModelScope.launch {
            settingsRepository.saveTiming(value)
            updateSettings()
        }
    }

    fun previewVoice(voiceType: VoiceType) {
        val previewMessage = when (voiceType) {
            VoiceType.TSUNDERE -> "뭐야? 나랑 같이 가고 싶은 거야? 흥, 딱히 널 기다린 건 아니니까 착각하지 마!"
            VoiceType.REALISTIC -> "안녕? 오늘도 지각 안 하려고 노력 중이지? 언니가 확실히 챙겨줄게. 빨리 가자!"
            VoiceType.AI -> "시스템 활성화. 사용자님, 효율적인 루틴 수행을 위해 제가 최적의 가이드를 제공하겠습니다."
        }
        ttsManager.speak(previewMessage, voiceType)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.signOut()
            routineRepository.clearAllLocalData()
            onComplete()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.deleteAccount()
            result.onSuccess {
                routineRepository.clearAllLocalData()
                onSuccess()
            }.onFailure { e ->
                onError("회원탈퇴 실패: ${e.message}")
            }
        }
    }
}