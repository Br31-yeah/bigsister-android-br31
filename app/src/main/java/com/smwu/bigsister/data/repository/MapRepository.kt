package com.smwu.bigsister.data.repository

import android.util.Log
import com.smwu.bigsister.data.network.ODsayResponse
import com.smwu.bigsister.data.network.ODsayService
import com.smwu.bigsister.data.network.StationInfo
import javax.inject.Inject

class MapRepository @Inject constructor(
    private val odsayService: ODsayService
) {
    // ▼▼▼ 여기에 본인의 ODsay API 키를 꼭 넣으세요! ▼▼▼
    private val apiKey = "Zh6lUheHIgm8yCwMWb2+R3f221p2+hbaS3CP6CftEJU"
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    // 1. (기존 기능) 원본 응답 통째로 받기
    suspend fun searchPath(
        startX: Double, startY: Double, endX: Double, endY: Double
    ): ODsayResponse {
        return odsayService.getTransitPath(apiKey, startX, startY, endX, endY)
    }

    // 2. (추가된 기능) "127.xxx,37.xxx" 문자열을 받아서 -> 예상 소요시간(분)만 리턴하기
    suspend fun getExpectedDuration(fromString: String, toString: String): Int {
        try {
            // "127.0276,37.4979" 처럼 콤마로 된 문자열을 분리합니다.
            val startParts = fromString.split(",")
            val endParts = toString.split(",")

            if (startParts.size < 2 || endParts.size < 2) return 0

            val startX = startParts[0].trim().toDouble() // 경도
            val startY = startParts[1].trim().toDouble() // 위도
            val endX = endParts[0].trim().toDouble()
            val endY = endParts[1].trim().toDouble()

            // API 호출
            val response = odsayService.getTransitPath(
                apiKey = apiKey,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY
            )

            // 결과에서 첫 번째 경로의 총 시간(totalTime)만 쏙 뽑아서 줍니다.
            return response.result?.path?.firstOrNull()?.info?.totalTime ?: 0

        } catch (e: Exception) {
            Log.e("MapRepository", "시간 계산 실패: ${e.message}")
            return 0
        }
    }

    // ▼▼▼ [추가] 역 이름으로 검색해서 결과 리스트 주기 ▼▼▼
    suspend fun searchStationByName(name: String): List<StationInfo> {
        return try {
            val response = odsayService.searchStation(
                apiKey = apiKey,
                stationName = name
            )
            // 결과가 없으면 빈 리스트 반환
            response.result?.station ?: emptyList()
        } catch (e: Exception) {
            Log.e("MapRepository", "역 검색 실패: ${e.message}")
            emptyList()
        }
    }

}