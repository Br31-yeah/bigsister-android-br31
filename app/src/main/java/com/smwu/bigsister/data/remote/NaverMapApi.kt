package com.smwu.bigsister.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 네이버 지도 API 엔드포인트 정의
 */
interface NaverMapApi {

    /**
     * 자동차 길찾기 (Driving Directions 5)
     * [GET] /map-direction/v1/driving
     */
    @GET("map-direction/v1/driving")
    suspend fun getDrivingDirections(
        @Query("start") start: String, // "경도,위도" (예: "127.123,37.123")
        @Query("goal") goal: String,
        @Query("option") option: String = "traavoidcaronly" // 실시간 빠른 길
    ): MapResponse // MapResponse 형태로 응답을 받음
}