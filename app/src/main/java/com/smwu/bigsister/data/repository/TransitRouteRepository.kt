package com.smwu.bigsister.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.smwu.bigsister.BuildConfig
import com.smwu.bigsister.data.network.GoogleRoutesRequest
import com.smwu.bigsister.data.network.GoogleRoutesService
import com.smwu.bigsister.data.network.LatLngValue
import com.smwu.bigsister.data.network.LocationLatLng
import com.smwu.bigsister.data.network.Route
import com.smwu.bigsister.data.network.RouteLocation
import javax.inject.Inject

class TransitRouteRepository @Inject constructor(
    private val googleRoutesService: GoogleRoutesService
) {

    // ✅ Routes API 전용 키 사용
    private val apiKey = BuildConfig.ROUTES_API_KEY

    /**
     * Google Routes API – 대중교통 다중 경로 조회
     *
     * @return routes 리스트
     */
    suspend fun getTransitRoutes(
        origin: LatLng,
        destination: LatLng
    ): List<Route> {
        return try {

            // 🔍 키가 실제로 들어왔는지 확인 (디버깅용)
            Log.d("KEY_CHECK", "ROUTES_API_KEY=$apiKey")

            val request = GoogleRoutesRequest(
                origin = RouteLocation(
                    LocationLatLng(
                        LatLngValue(
                            latitude = origin.latitude,
                            longitude = origin.longitude
                        )
                    )
                ),
                destination = RouteLocation(
                    LocationLatLng(
                        LatLngValue(
                            latitude = destination.latitude,
                            longitude = destination.longitude
                        )
                    )
                )
                // travelMode = TRANSIT (기본값)
                // computeAlternativeRoutes = true (기본값)
            )

            val response = googleRoutesService.computeRoutes(
                apiKey = apiKey,
                fieldMask = "routes.localizedValues,routes.legs.steps.transitDetails",
                request = request
            )

            Log.d(
                "TransitRouteRepo",
                "routes.size = ${response.routes.size}"
            )

            response.routes

        } catch (e: Exception) {
            Log.e(
                "TransitRouteRepo",
                "Google Routes API 호출 실패",
                e
            )
            emptyList()
        }
    }
}