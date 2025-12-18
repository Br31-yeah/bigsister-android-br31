package com.smwu.bigsister.data.network

// 일반 장소 검색 결과용
data class GooglePlaceResponse(
    val results: List<PlaceResult>,
    val status: String
)

data class PlaceResult(
    val name: String,
    val formatted_address: String,
    val geometry: PlaceGeometry
)

data class PlaceGeometry(val location: PlaceLocation)
data class PlaceLocation(val lat: Double, val lng: Double)

// 현위치 좌표 -> 주소 변환용
data class GeocodingResponse(
    val results: List<GeocodingResult>,
    val status: String
)

data class GeocodingResult(
    val formatted_address: String
)