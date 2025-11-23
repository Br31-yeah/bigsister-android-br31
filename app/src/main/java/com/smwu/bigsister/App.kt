package com.smwu.bigsister

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk // 1. 이 줄이 추가되어야 합니다.
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt가 의존성 주입을 시작할 베이스 Application 클래스입니다.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 2. 카카오맵 SDK 초기화
        // (Manifest에 키가 있어도, 여기서 한 번 더 확실하게 해주는 것이 안전합니다)
        KakaoMapSdk.init(this, "193bcb7e7db09caeed23fbacfac6f358")
    }
}