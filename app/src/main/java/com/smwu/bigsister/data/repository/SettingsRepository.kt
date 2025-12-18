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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val firestore: FirebaseFirestore, // ✅ 추가
    private val auth: FirebaseAuth            // ✅ 추가
) {
    private object Keys {
        val SISTER_TYPE = stringPreferencesKey("sister_type")
        val PUSH_ALARM = booleanPreferencesKey("push_alarm")
        val VOICE_ALARM = booleanPreferencesKey("voice_alarm")
        val INTENSITY = stringPreferencesKey("intensity")
        val TIMING = stringPreferencesKey("timing")
    }

    // --- 로컬 DataStore 데이터 읽기 ---
    val sisterType: Flow<String> = dataStore.data.map { it[Keys.SISTER_TYPE] ?: "TSUNDERE" }
    val pushAlarm: Flow<Boolean> = dataStore.data.map { it[Keys.PUSH_ALARM] ?: true }
    val voiceAlarm: Flow<Boolean> = dataStore.data.map { it[Keys.VOICE_ALARM] ?: false }
    val intensity: Flow<String> = dataStore.data.map { it[Keys.INTENSITY] ?: "보통 - 표준 알림" }
    val timing: Flow<String> = dataStore.data.map { it[Keys.TIMING] ?: "마감 3분 전" }

    // --- Firebase 서버에 설정 저장 ---
    suspend fun uploadSettingsToFirebase(settings: Map<String, Any>) {
        val uid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(uid)
                .collection("settings").document("config")
                .set(settings).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Firebase 서버에서 설정 가져와서 로컬에 덮어쓰기 ---
    suspend fun syncFromFirebase() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("settings").document("config").get().await()

            if (snapshot.exists()) {
                dataStore.edit { prefs ->
                    snapshot.getString("sisterType")?.let { prefs[Keys.SISTER_TYPE] = it }
                    snapshot.getBoolean("pushAlarm")?.let { prefs[Keys.PUSH_ALARM] = it }
                    snapshot.getBoolean("voiceAlarm")?.let { prefs[Keys.VOICE_ALARM] = it }
                    snapshot.getString("intensity")?.let { prefs[Keys.INTENSITY] = it }
                    snapshot.getString("timing")?.let { prefs[Keys.TIMING] = it }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- 로컬 개별 저장 함수들 ---
    suspend fun saveSisterType(type: String) { dataStore.edit { it[Keys.SISTER_TYPE] = type } }
    suspend fun savePushAlarm(isEnabled: Boolean) { dataStore.edit { it[Keys.PUSH_ALARM] = isEnabled } }
    suspend fun saveVoiceAlarm(isEnabled: Boolean) { dataStore.edit { it[Keys.VOICE_ALARM] = isEnabled } }
    suspend fun saveIntensity(value: String) { dataStore.edit { it[Keys.INTENSITY] = value } }
    suspend fun saveTiming(value: String) { dataStore.edit { it[Keys.TIMING] = value } }
}