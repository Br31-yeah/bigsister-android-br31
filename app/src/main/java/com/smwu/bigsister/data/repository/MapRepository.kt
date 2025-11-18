package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.remote.NaverMapApi
import javax.inject.Inject

/**
 * 지도/경로 관련 API 호출을 담당하는 Repository
 */
class MapRepository @Inject constructor(
    private val naverMapApi: NaverMapApi
) {
    /**
     * 출발지와 도착지(경도,위도)를 받아 예상 소요 시간(분)을 반환합니다.
     */
    suspend fun getExpectedDuration(start: String, goal: String): Int {
        return try {
            val response = naverMapApi.getDrivingDirections(start, goal)
            if (response.code == 0 && response.route?.traavoidcaronly?.isNotEmpty() == true) {
                val durationInMillis = response.route.traavoidcaronly[0].summary.duration
                // 밀리초(ms) -> 분(min)으로 변환 (올림 처리)
                (durationInMillis / (1000 * 60)).toInt() + 1
            } else {
                -1 // API 에러
            }
        } catch (e: Exception) {
            -1 // 네트워크 에러
        }
    }
}