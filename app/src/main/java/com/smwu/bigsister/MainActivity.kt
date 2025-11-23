package com.smwu.bigsister

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.smwu.bigsister.ui.KakaoMapTest
import com.smwu.bigsister.ui.theme.BigSisterTheme
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ▼▼▼ [수정됨] 해시 키 구하기 (Null 체크 추가) ▼▼▼
        try {
            // GET_SIGNATURES가 옛날 방식이라 경고가 뜰 수 있지만 무시(@Suppress)합니다.
            @Suppress("DEPRECATION")
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)

            val signatures = info.signatures
            // "값이 비어있지 않다면" 실행하라는 안전장치를 추가했습니다.
            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    // 로그캣에서 "해시키" 라고 검색하세요.
                    Log.d("KeyHash", "해시키: $keyHash")
                }
            }
        } catch (e: Exception) {
            Log.e("KeyHash", "해시키를 못 찾았습니다", e)
        }
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        enableEdgeToEdge()
        setContent {
            BigSisterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 테스트용 지도 화면
                    KakaoMapTest()
                }
            }
        }
    }
}