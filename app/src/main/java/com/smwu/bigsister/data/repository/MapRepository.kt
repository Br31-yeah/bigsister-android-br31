package com.smwu.bigsister.data.repository

import android.util.Log
import com.smwu.bigsister.data.network.ODsayResponse
import com.smwu.bigsister.data.network.ODsayService
import com.smwu.bigsister.data.network.StationInfo
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val odsayService: ODsayService
) {
    // ⚠️ 실제 배포 전에는 반드시 local.properties / BuildConfig로 이동
    private val apiKey = "Zh6lUheHIgm8yCwMWb2+R3f221p2+hbaS3CP6CftEJU"

    /**
     * ODsay 경로 전체 응답
     */
    suspend fun searchPath(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double
    ): ODsayResponse {
        return odsayService.getTransitPath(
            apiKey = apiKey,
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY
        )
    }

    /**
     * "127.xxx,37.xxx" 형태의 좌표 문자열을 받아
     * 👉 예상 소요시간 (분) 을 Long 으로 반환
     */
    suspend fun getExpectedDuration(
        fromString: String,
        toString: String
    ): Long {
        return try {
            val startParts = fromString.split(",")
            val endParts = toString.split(",")

            if (startParts.size < 2 || endParts.size < 2) return 0L

            val startX = startParts[0].trim().toDouble()
            val startY = startParts[1].trim().toDouble()
            val endX = endParts[0].trim().toDouble()
            val endY = endParts[1].trim().toDouble()

            val response = odsayService.getTransitPath(
                apiKey = apiKey,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY
            )

            response.result
                ?.path
                ?.firstOrNull()
                ?.info
                ?.totalTime
                ?.toLong()
                ?: 0L

        } catch (e: Exception) {
            Log.e("MapRepository", "ODsay 시간 계산 실패", e)
            0L
        }
    }

    /**
     * 역 이름 검색
     */
    suspend fun searchStationByName(name: String): List<StationInfo> {
        return try {
            val response = odsayService.searchStation(
                apiKey = apiKey,
                stationName = name
            )
            response.result?.station ?: emptyList()
        } catch (e: Exception) {
            Log.e("MapRepository", "역 검색 실패", e)
            emptyList()
        }
    }
}