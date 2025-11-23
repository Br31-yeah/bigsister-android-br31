package com.smwu.bigsister

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt가 의존성 주입을 시작할 베이스 Application 클래스입니다.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 🗑️ 카카오맵 초기화 코드 삭제됨
        // 이제 아무것도 안 적어도 됩니다!
    }
}