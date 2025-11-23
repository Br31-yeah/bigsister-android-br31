package com.smwu.bigsister.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
fun KakaoMapTest() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                start(
                    // 첫 번째 인자: 지도 생명주기 콜백 (필수)
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            // 지도가 사라질 때 할 일 (지금은 비워둬도 됨)
                            Log.d("KakaoMap", "Map Destroyed")
                        }

                        override fun onMapError(error: Exception?) {
                            // 에러 났을 때 할 일
                            Log.e("KakaoMap", "Map Error: ${error?.message}")
                        }
                    },
                    // 두 번째 인자: 지도 로딩 완료 콜백
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            // 지도가 성공적으로 떴을 때!
                            Log.d("KakaoMap", "Map Ready!")
                        }
                    }
                )
            }
        }
    )
}