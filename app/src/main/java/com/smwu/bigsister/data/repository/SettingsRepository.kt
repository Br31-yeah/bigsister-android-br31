package com.smwu.bigsister.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// com/smwu/bigsister/data/repository/SettingsRepository.kt

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private object Keys {
        val SISTER_TYPE = stringPreferencesKey("sister_type")
        val PUSH_ALARM = booleanPreferencesKey("push_alarm")
        val VOICE_ALARM = booleanPreferencesKey("voice_alarm")
        val INTENSITY = stringPreferencesKey("intensity")
        val TIMING = stringPreferencesKey("timing")
        // ✅ 온보딩 완료 여부 키 추가
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    // --- 온보딩 읽기 ---
    val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }

    // --- 온보딩 저장 ---
    suspend fun saveOnboardingSeen(seen: Boolean) {
        dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = seen }
    }

    // ... 기존 코드 유지 (sisterType, pushAlarm 등)
    val sisterType: Flow<String> = dataStore.data.map { it[Keys.SISTER_TYPE] ?: "TSUNDERE" }
    val pushAlarm: Flow<Boolean> = dataStore.data.map { it[Keys.PUSH_ALARM] ?: true }
    val voiceAlarm: Flow<Boolean> = dataStore.data.map { it[Keys.VOICE_ALARM] ?: false }
    val intensity: Flow<String> = dataStore.data.map { it[Keys.INTENSITY] ?: "보통 - 표준 알림" }
    val timing: Flow<String> = dataStore.data.map { it[Keys.TIMING] ?: "마감 3분 전" }

    suspend fun uploadSettingsToFirebase(settings: Map<String, Any>) { /* 기존 동일 */ }
    suspend fun syncFromFirebase() { /* 기존 동일 */ }
    suspend fun saveSisterType(type: String) { dataStore.edit { it[Keys.SISTER_TYPE] = type } }
    suspend fun savePushAlarm(isEnabled: Boolean) { dataStore.edit { it[Keys.PUSH_ALARM] = isEnabled } }
    suspend fun saveVoiceAlarm(isEnabled: Boolean) { dataStore.edit { it[Keys.VOICE_ALARM] = isEnabled } }
    suspend fun saveIntensity(value: String) { dataStore.edit { it[Keys.INTENSITY] = value } }
    suspend fun saveTiming(value: String) { dataStore.edit { it[Keys.TIMING] = value } }
}