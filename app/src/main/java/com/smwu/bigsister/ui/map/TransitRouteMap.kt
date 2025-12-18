package com.smwu.bigsister.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.smwu.bigsister.data.model.transit.TransitRouteUiModel

@Composable
fun TransitRouteMap(
    origin: LatLng,
    destination: LatLng,
    selectedRoute: TransitRouteUiModel?
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(origin, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

        selectedRoute?.encodedPolylines?.forEach { encodedPolyline ->
            val decodedPoints: List<LatLng> =
                try {
                    PolyUtil.decode(encodedPolyline)
                } catch (e: Exception) {
                    emptyList()
                }

            if (decodedPoints.isNotEmpty()) {
                Polyline(
                    points = decodedPoints,
                    width = 14f
                )
            }
        }
    }
}