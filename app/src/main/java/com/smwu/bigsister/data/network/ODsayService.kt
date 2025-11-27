package com.smwu.bigsister.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ODsayService {
    // 대중교통 길찾기 요청 (SearchPubTransPathT)
    @GET("searchPubTransPathT")
    suspend fun getTransitPath(
        @Query("apiKey") apiKey: String, // 발급받은 키
        @Query("SX") startX: Double,     // 출발지 경도 (Longitude)
        @Query("SY") startY: Double,     // 출발지 위도 (Latitude)
        @Query("EX") endX: Double,       // 도착지 경도
        @Query("EY") endY: Double,       // 도착지 위도
        @Query("lang") lang: Int = 0     // 0:국문, 1:영문
    ): ODsayResponse

    // ▼▼▼ [추가] 지하철역 검색 기능 ▼▼▼
    @GET("searchStation")
    suspend fun searchStation(
        @Query("apiKey") apiKey: String,
        @Query("stationName") stationName: String, // 예: "강남"
        @Query("CID") cityCode: Int = 1000,        // 1000: 수도권
        @Query("stationClass") stationClass: Int = 2, // 2: 지하철
        @Query("displayCnt") displayCnt: Int = 10  // 10개만 보여줘
    ): ODsayStationResponse // 👈 이걸 새로 만들어야 합니다 (2단계에서)

}