package com.smwu.bigsister.data.repository

import android.util.Log
import com.smwu.bigsister.BuildConfig
import com.smwu.bigsister.data.network.GoogleDirectionsService
import com.smwu.bigsister.data.network.ODsayResponse
import com.smwu.bigsister.data.network.ODsayService
import com.smwu.bigsister.data.network.StationInfo
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val odsayService: ODsayService,
    private val googleService: GoogleDirectionsService
) {

    /* ────────────────────────────────
       API Keys (BuildConfig)
    ──────────────────────────────── */

    private val odsayApiKey: String = BuildConfig.ODSAY_API_KEY
    private val googleApiKey: String = BuildConfig.GOOGLE_MAPS_API_KEY

    /* ────────────────────────────────
       ODsay : 경로 전체 응답
    ──────────────────────────────── */

    suspend fun searchPath(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double
    ): ODsayResponse {
        return odsayService.getTransitPath(
            apiKey = odsayApiKey,
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY
        )
    }

    /* ────────────────────────────────
       ODsay : 대중교통 소요시간 (분)
    ──────────────────────────────── */

    suspend fun getExpectedDuration(
        fromString: String,
        toString: String
    ): Long {
        return try {
            val start = fromString.split(",")
            val end = toString.split(",")

            if (start.size < 2 || end.size < 2) return 0L

            val response = odsayService.getTransitPath(
                apiKey = odsayApiKey,
                startX = start[0].trim().toDouble(),
                startY = start[1].trim().toDouble(),
                endX = end[0].trim().toDouble(),
                endY = end[1].trim().toDouble()
            )

            response.result
                ?.path
                ?.firstOrNull()
                ?.info
                ?.totalTime
                ?.toLong()
                ?: 0L

        } catch (e: Exception) {
            Log.e("MapRepository", "ODsay 이동 시간 계산 실패", e)
            0L
        }
    }

    /* ────────────────────────────────
       Google Directions : 도보 / 자동차
       (lat,lng 문자열 기준, 분 단위)
    ──────────────────────────────── */

    suspend fun getWalkingOrDrivingDuration(
        fromLatLng: String, // "lat,lng"
        toLatLng: String,
        mode: String        // "walking" | "driving"
    ): Long {
        return try {
            val response = googleService.getDirections(
                origin = fromLatLng,
                destination = toLatLng,
                mode = mode,
                apiKey = googleApiKey
            )

            response.routes
                .firstOrNull()
                ?.legs
                ?.firstOrNull()
                ?.duration
                ?.value
                ?.div(60)   // seconds → minutes
                ?.toLong()
                ?: 0L

        } catch (e: Exception) {
            Log.e("MapRepository", "Google 이동 시간 계산 실패", e)
            0L
        }
    }

    /* ────────────────────────────────
       ODsay : 지하철역 검색
    ──────────────────────────────── */

    suspend fun searchStationByName(name: String): List<StationInfo> {
        Log.d(
            "ODSAY_KEY_CHECK",
            "BuildConfig.ODSAY_API_KEY = '${BuildConfig.ODSAY_API_KEY}'"
        )

        return try {
            val response = odsayService.searchStation(
                apiKey = BuildConfig.ODSAY_API_KEY,
                stationName = name
            )

            Log.d("ODSAY_RESPONSE", "response = $response")

            response.result?.station ?: emptyList()
        } catch (e: Exception) {
            Log.e("MapRepository", "ODsay 역 검색 실패", e)
            emptyList()
        }
    }
}