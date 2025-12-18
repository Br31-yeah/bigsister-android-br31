package com.smwu.bigsister.data.repository

import android.util.Log
import com.smwu.bigsister.BuildConfig
import com.smwu.bigsister.data.model.transit.GeoPoint
import com.smwu.bigsister.data.network.GoogleDirectionsService
import com.smwu.bigsister.data.network.ODsayService
import com.smwu.bigsister.data.network.StationInfo
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val odsayService: ODsayService,
    private val googleService: GoogleDirectionsService
) : MapRepository {

    override suspend fun getWalkingOrDrivingDuration(from: GeoPoint, to: GeoPoint, mode: String): Long {
        return try {
            googleService.getDirections(
                origin = "${from.lat},${from.lng}",
                destination = "${to.lat},${to.lng}",
                mode = mode,
                apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            ).routes.firstOrNull()?.legs?.firstOrNull()?.duration?.value?.div(60)?.toLong() ?: 0L
        } catch (e: Exception) { 0L }
    }

    override suspend fun getTransitDuration(from: GeoPoint, to: GeoPoint): Long {
        return try {
            odsayService.getTransitPath(
                apiKey = BuildConfig.ODSAY_API_KEY,
                startX = from.lng, startY = from.lat, endX = to.lng, endY = to.lat
            ).result?.path?.firstOrNull()?.info?.totalTime?.toLong() ?: 0L
        } catch (e: Exception) { 0L }
    }

    // ✅ 일반 장소 검색 (Google Places API)
    override suspend fun searchPlacesByName(name: String): List<StationInfo> {
        return try {
            val response = googleService.searchPlaces(
                query = name,
                apiKey = BuildConfig.GOOGLE_MAPS_API_KEY // 👈 2단계 설정이 되어야 여기가 채워짐
            )

            response.results.map { place ->
                StationInfo(
                    stationName = place.name,
                    laneName = place.formatted_address,
                    x = place.geometry.location.lng,
                    y = place.geometry.location.lat,
                    stationID = 0
                )
            }
        } catch (e: Exception) {
            Log.e("MapRepo", "Search Failed", e)
            emptyList()
        }
    }

    // ✅ 현위치 좌표 -> 지명 주소 변환 (Geocoding API)
    suspend fun getAddressFromLatLng(lat: Double, lng: Double): String {
        return try {
            val response = googleService.reverseGeocode(
                latlng = "$lat,$lng",
                apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
            )
            response.results.firstOrNull()?.formatted_address?.replace("대한민국 ", "") ?: "현위치"
        } catch (e: Exception) {
            "현위치"
        }
    }
}