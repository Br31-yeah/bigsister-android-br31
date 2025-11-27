package com.smwu.bigsister.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GoogleMapScreen() {
    // 초기 카메라 위치 설정 (예: 서울 시청)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
    }

    // AndroidView 없이 바로 GoogleMap 컴포넌트 사용 가능!
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        // 나중에 여기에 마커(Marker)나 경로(Polyline)를 컴포즈처럼 넣으면 됩니다.
        // Marker(state = MarkerState(position = LatLng(...)))
    }
}