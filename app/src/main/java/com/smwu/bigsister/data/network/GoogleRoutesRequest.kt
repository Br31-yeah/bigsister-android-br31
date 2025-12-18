package com.smwu.bigsister.data.network

//요청 바디를 1:1로 표현하는 순수 데이터 클래스
//Google Routes API 문서를 그대로 Kotlin 구조로 옮김

/**
 * Google Routes API - Compute Routes Request
 * https://routes.googleapis.com/directions/v2:computeRoutes
 */
data class GoogleRoutesRequest(
    val origin: RouteLocation,
    val destination: RouteLocation,
    val travelMode: String = "TRANSIT",
    val computeAlternativeRoutes: Boolean = true,
    val transitPreferences: TransitPreferences? = null,
    val departureTime: String? = null
)
/* ───────── 위치 ───────── */

data class RouteLocation(
    val location: LocationLatLng
)

data class LocationLatLng(
    val latLng: LatLngValue
)

data class LatLngValue(
    val latitude: Double,
    val longitude: Double
)

/* ───────── 대중교통 옵션 ───────── */

data class TransitPreferences(
    val routingPreference: String? = null, // LESS_WALKING | FEWER_TRANSFERS
    val allowedTravelModes: List<String>? = null // BUS, SUBWAY, TRAIN ...
)