package com.smwu.bigsister.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // DataStore에서 사용할 '키' (Key) 정의
    private object Keys {
        val SISTER_TYPE = stringPreferencesKey("sister_type")
        val PUSH_ALARM = booleanPreferencesKey("push_alarm")
        val VOICE_ALARM = booleanPreferencesKey("voice_alarm")
        // TODO: 알림 강도, 타이밍 등 [cite: 462-465]
    }

    // '언니 타입' 읽기 (Flow)
    val sisterType: Flow<String> = dataStore.data.map { preferences ->
        preferences[Keys.SISTER_TYPE] ?: "츤데레" // 기본값 "츤데레"
    }

    // '푸시 알림' 설정 읽기 (Flow)
    val pushAlarm: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.PUSH_ALARM] ?: true // 기본값 '켜기'
    }

    // '음성 알림' 설정 읽기 (Flow)
    val voiceAlarm: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[Keys.VOICE_ALARM] ?: false // 기본값 '끄기'
    }

    // '언니 타입' 저장
    suspend fun saveSisterType(type: String) {
        dataStore.edit { preferences ->
            preferences[Keys.SISTER_TYPE] = type
        }
    }

    // '푸시 알림' 설정 저장
    suspend fun savePushAlarm(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PUSH_ALARM] = isEnabled
        }
    }

    // '음성 알림' 설정 저장
    suspend fun saveVoiceAlarm(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.VOICE_ALARM] = isEnabled
        }
    }
}