package com.smwu.bigsister.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleDirectionsService {

    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,          // walking | driving
        @Query("key") apiKey: String
    ): GoogleDirectionsResponse
}