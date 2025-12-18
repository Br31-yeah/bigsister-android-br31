package com.smwu.bigsister.data.network

data class GoogleRoutesRequest(
    val origin: RouteLocation,
    val destination: RouteLocation,
    val travelMode: String = "TRANSIT",
    val computeAlternativeRoutes: Boolean = true,
    val transitPreferences: TransitPreferences? = null,
    val departureTime: String? = null
)

data class RouteLocation(
    val location: LocationLatLng
)

data class LocationLatLng(
    val latLng: LatLngValue // 아래 클래스를 참조
) {
    // ✅ 클래스 내부로 이동하여 이름 충돌 방지
    data class LatLngValue(
        val latitude: Double,
        val longitude: Double
    )
}

data class TransitPreferences(
    val routingPreference: String? = null,
    val allowedTravelModes: List<String>? = null
)