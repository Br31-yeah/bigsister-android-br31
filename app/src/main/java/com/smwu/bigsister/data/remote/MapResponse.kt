package com.smwu.bigsister.data.remote

import com.google.gson.annotations.SerializedName


/**
 * 네이버 지도 Directions 5 API의 응답(Response) 데이터 모델
 */
data class MapResponse(
    @SerializedName("code")
    val code: Int, // 0이면 성공
    @SerializedName("message")
    val message: String,
    @SerializedName("route")
    val route: Route? // 경로 요약 정보
)

data class Route(
    @SerializedName("traavoidcaronly") // "traavoidcaronly"는 오타가 아님 (API 명세)
    val traavoidcaronly: List<RouteSummary>?
)

data class RouteSummary(
    @SerializedName("summary")
    val summary: Summary
)

data class Summary(
    @SerializedName("duration")
    val duration: Long, // 총 소요 시간 (밀리초, 1/1000초)
    @SerializedName("distance")
    val distance: Long // 총 거리 (미터)
)