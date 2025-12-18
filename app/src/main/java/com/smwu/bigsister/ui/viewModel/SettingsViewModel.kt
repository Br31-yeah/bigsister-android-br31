
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val routineRepository: RoutineRepository,
    private val ttsManager: TtsManager // ✅ TTS 매니저 추가 주입
) : ViewModel() {

    // ────────────────────────────
    // 기존 설정 (DataStore)
    // ────────────────────────────
    val sisterType: StateFlow<String> = settingsRepository.sisterType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TSUNDERE")

    val pushAlarm: StateFlow<Boolean> = settingsRepository.pushAlarm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val voiceAlarm: StateFlow<Boolean> = settingsRepository.voiceAlarm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setSisterType(type: String) {
        viewModelScope.launch { settingsRepository.saveSisterType(type) }
    }

    fun setPushAlarm(isEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.savePushAlarm(isEnabled) }
    }

    fun setVoiceAlarm(isEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveVoiceAlarm(isEnabled) }
    }

    // ────────────────────────────
    // 🔊 [추가] 미리듣기 기능
    // ────────────────────────────
    fun previewVoice(voiceType: VoiceType) {
        val previewMessage = when (voiceType) {
            VoiceType.TSUNDERE -> "뭐야? 나랑 같이 가고 싶은 거야? 흥, 딱히 널 기다린 건 아니니까 착각하지 마!"
            VoiceType.REALISTIC -> "안녕? 오늘도 지각 안 하려고 노력 중이지? 언니가 확실히 챙겨줄게. 빨리 가자!"
            VoiceType.AI -> "시스템 활성화. 사용자님, 효율적인 루틴 수행을 위해 제가 최적의 가이드를 제공하겠습니다."
        }
        ttsManager.speak(previewMessage, voiceType)
    }

    // ────────────────────────────
    // 로그아웃 및 회원탈퇴
    // ────────────────────────────

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
