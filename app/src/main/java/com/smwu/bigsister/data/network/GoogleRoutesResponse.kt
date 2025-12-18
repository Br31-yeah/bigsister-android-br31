package com.smwu.bigsister.data.network

/**
 * Google Routes API v2 응답 모델
 * 필요한 필드만 우선 정의 (FieldMask 기반)
 */

data class GoogleRoutesResponse(
    val routes: List<Route>
)

/* ────────────────────────────────
   Route (경로 1개)
──────────────────────────────── */

data class Route(
    val legs: List<RouteLeg>,
    val localizedValues: LocalizedValues? = null
)

/* ────────────────────────────────
   Leg (출발 → 도착 전체 구간)
──────────────────────────────── */

data class RouteLeg(
    val steps: List<RouteStep>
)

/* ────────────────────────────────
   Step (도보 / 대중교통 단위)
──────────────────────────────── */

data class RouteStep(
    val travelMode: String, // WALKING | TRANSIT
    val transitDetails: TransitDetails? = null
)

/* ────────────────────────────────
   대중교통 상세
──────────────────────────────── */

data class TransitDetails(
    val stopDetails: StopDetails?,
    val transitLine: TransitLine?,
    val stopCount: Int?
)

data class StopDetails(
    val departureStop: Stop?,
    val arrivalStop: Stop?
)

data class Stop(
    val name: String?
)

/* ────────────────────────────────
   노선 정보
──────────────────────────────── */

data class TransitLine(
    val name: String?,
    val nameShort: String?,
    val vehicle: TransitVehicle?
)

data class TransitVehicle(
    val type: String // BUS | SUBWAY | TRAIN
)

/* ────────────────────────────────
   현지화된 값 (총 시간 등)
──────────────────────────────── */

data class LocalizedValues(
    val duration: LocalizedTime? = null,
    val distance: LocalizedDistance? = null
)

data class LocalizedTime(
    val text: String // 예: "45분"
)

data class LocalizedDistance(
    val text: String // 예: "12.3km"
)