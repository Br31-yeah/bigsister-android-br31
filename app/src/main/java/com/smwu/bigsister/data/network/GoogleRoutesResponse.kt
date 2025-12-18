package com.smwu.bigsister.data.network

data class GoogleRoutesResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<RouteLeg>,
    val localizedValues: LocalizedValues? = null
)

data class RouteLeg(
    val steps: List<RouteStep>,
    val staticDuration: String? = null, // ✅ 추가: 전체 소요 시간 (초 단위 문자열 "3600s")
    val localizedValues: LocalizedValues? = null
)

data class RouteStep(
    val travelMode: String,
    val staticDuration: String? = null, // ✅ 추가: 단계별 소요 시간
    val transitDetails: TransitDetails? = null,
    val polyline: PolylineValue? = null
)

data class PolylineValue(
    val encodedPolyline: String? = null
)

data class TransitDetails(
    val stopDetails: StopDetails? = null,
    val transitLine: TransitLine? = null,
    val stopCount: Int? = null
)

data class StopDetails(
    val departureStop: Stop? = null,
    val arrivalStop: Stop? = null
)

data class Stop(
    val name: String? = null
)

data class TransitLine(
    val name: String? = null,
    val nameShort: String? = null,
    val vehicle: TransitVehicle? = null
)

data class TransitVehicle(
    val type: String
)

data class LocalizedValues(
    // ✅ 로그 분석 결과 localizedValues 내의 필드들은 객체 형태입니다.
    val duration: LocalizedText? = null,
    val distance: LocalizedText? = null
)

data class LocalizedText(
    val text: String
)