package com.smwu.bigsister.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDirectionsService {

    // 경로 검색 (기존)
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String
    ): GoogleDirectionsResponse

    // ✅ 추가: 모든 장소 검색 (Google Places API)
    @GET("https://maps.googleapis.com/maps/api/place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "ko"
    ): GooglePlaceResponse

    // ✅ 추가: 좌표를 주소로 변환 (Google Geocoding API)
    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    suspend fun reverseGeocode(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "ko"
    ): GeocodingResponse
}