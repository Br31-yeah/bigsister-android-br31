package com.smwu.bigsister.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GoogleRoutesService {

    @POST("directions/v2:computeRoutes")
    suspend fun computeRoutes(
        @Header("X-Goog-Api-Key") apiKey: String,
        @Header("X-Goog-FieldMask") fieldMask: String,
        @Body request: GoogleRoutesRequest
    ): GoogleRoutesResponse
}