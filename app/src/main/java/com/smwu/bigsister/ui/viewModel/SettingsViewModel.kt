package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.SettingsRepository
import com.smwu.bigsister.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,      // ✅ 추가됨
    private val routineRepository: RoutineRepository // ✅ 추가됨
) : ViewModel() {

    // ────────────────────────────
    // 기존 설정 (DataStore)
    // ────────────────────────────
    val sisterType: StateFlow<String> = settingsRepository.sisterType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "츤데레")

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
    // ✅ [추가] 로그아웃 및 회원탈퇴
    // ────────────────────────────

    // 로그아웃: Firebase signOut + 로컬 데이터 삭제
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userRepository.signOut() // Auth 로그아웃 & 유저 정보 삭제
            routineRepository.clearAllLocalData() // 루틴 데이터 삭제
            onComplete()
        }
    }

    // 회원탈퇴: Firebase 계정 삭제 + 로컬 데이터 삭제
    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            // 1. 계정 삭제 시도
            val result = userRepository.deleteAccount()

            result.onSuccess {
                // 2. 성공 시 로컬 루틴 데이터도 삭제
                routineRepository.clearAllLocalData()
                onSuccess()
            }.onFailure { e ->
                // 실패 시 (예: 재로그인 필요)
                onError("회원탈퇴 실패: ${e.message}")
            }
        }
    }
}