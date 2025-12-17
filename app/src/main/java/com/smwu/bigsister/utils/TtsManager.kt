package com.smwu.bigsister.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import com.smwu.bigsister.data.model.VoiceType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.KOREAN
            isInitialized = true
        }
    }

    fun speak(text: String, voiceType: VoiceType) {
        if (!isInitialized) return

        // 캐릭터별로 속도나 피치를 조절하고 싶다면 여기서 설정 가능
        when (voiceType) {
            VoiceType.TSUNDERE -> { tts?.setPitch(1.2f); tts?.setSpeechRate(1.1f) }
            VoiceType.REALISTIC -> { tts?.setPitch(1.0f); tts?.setSpeechRate(1.0f) }
            VoiceType.AI -> { tts?.setPitch(0.8f); tts?.setSpeechRate(0.9f) }
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}