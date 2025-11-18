package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // DataStore에서 설정을 읽어와 UI 상태로 변환
    val sisterType: StateFlow<String> = settingsRepository.sisterType
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "츤데레")

    val pushAlarm: StateFlow<Boolean> = settingsRepository.pushAlarm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val voiceAlarm: StateFlow<Boolean> = settingsRepository.voiceAlarm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI에서 변경 사항을 저장할 때 호출되는 함수
    fun setSisterType(type: String) {
        viewModelScope.launch {
            settingsRepository.saveSisterType(type)
        }
    }

    fun setPushAlarm(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.savePushAlarm(isEnabled)
        }
    }

    fun setVoiceAlarm(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveVoiceAlarm(isEnabled)
        }
    }
}