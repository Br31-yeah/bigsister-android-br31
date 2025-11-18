package com.smwu.bigsister

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.smwu.bigsister.ui.AppNavigation
import com.smwu.bigsister.ui.theme.BigSisterTheme
import com.smwu.bigsister.utils.getAppKeyHash
import dagger.hilt.android.AndroidEntryPoint

/**
 * 앱의 메인 진입점(Activity)입니다.
 * [수정됨] Hilt가 이 Activity에 의존성을 주입할 수 있도록 @AndroidEntryPoint 추가
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAppKeyHash(this) // ✅ [추가] '키 해시'를 로그로 찍는 함수 호출

        enableEdgeToEdge()
        setContent {
            BigSisterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 예제 'Greeting' 대신, 'AppNavigation'을 실행합니다.
                    AppNavigation()
                }
            }
        }
    }
}