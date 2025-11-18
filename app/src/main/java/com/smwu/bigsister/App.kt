package com.smwu.bigsister

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt가 의존성 주입을 시작할 베이스 Application 클래스입니다.
 */
@HiltAndroidApp
class App : Application() {
    // (내부는 비어있어도 됩니다)
}