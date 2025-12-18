package com.smwu.bigsister.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.smwu.bigsister.BuildConfig
import com.smwu.bigsister.data.model.transit.TransitRouteUiModel
import com.smwu.bigsister.data.model.transit.toUiModel
import com.smwu.bigsister.data.network.GoogleRoutesRequest
import com.smwu.bigsister.data.network.GoogleRoutesService
import com.smwu.bigsister.data.network.LocationLatLng
import com.smwu.bigsister.data.network.RouteLocation
import javax.inject.Inject

class TransitRouteRepository @Inject constructor(
    private val googleRoutesService: GoogleRoutesService
) {
    private val apiKey = BuildConfig.ROUTES_API_KEY

    suspend fun getTransitRoutes(
        origin: LatLng,
        destination: LatLng,
        mode: String // ✅ mode 파라미터 추가
    ): List<TransitRouteUiModel> {
        return try {
            val request = GoogleRoutesRequest(
                origin = RouteLocation(LocationLatLng(LocationLatLng.LatLngValue(origin.latitude, origin.longitude))),
                destination = RouteLocation(LocationLatLng(LocationLatLng.LatLngValue(destination.latitude, destination.longitude))),
                travelMode = mode, // ✅ DRIVE, WALK, TRANSIT 반영
                computeAlternativeRoutes = true
            )

            val response = googleRoutesService.computeRoutes(
                apiKey = apiKey,
                fieldMask = "routes.legs.steps,routes.legs.duration,routes.legs.staticDuration,routes.legs.steps.travelMode,routes.legs.steps.transitDetails,routes.legs.steps.polyline,routes.localizedValues",
                request = request
            )

            response.routes.map { it.toUiModel() }
        } catch (e: Exception) {
            Log.e("TransitRouteRepo", "API 호출 실패: ${e.message}")
            emptyList()
        }
    }
}