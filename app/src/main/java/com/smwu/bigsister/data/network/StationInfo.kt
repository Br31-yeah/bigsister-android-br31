package com.smwu.bigsister.data.network

import com.smwu.bigsister.data.model.transit.GeoPoint

/**
 * 장소 및 역 정보를 담는 공용 데이터 클래스
 * [기본값 설정 이유]
 * ODsay API는 stationID를 주지만, Google Places API는 주지 않습니다.
 * stationID = 0으로 기본값을 설정하면 두 API 결과 모두 이 클래스 하나로 담을 수 있습니다.
 */
data class StationInfo(
    val stationName: String,
    val stationID: Int = 0,   // ✅ 기본값 설정으로 파라미터 누락 방지
    val laneName: String = "",
    val x: Double,            // longitude (경도)
    val y: Double             // latitude (위도)
)

/**
 * 좌표 변환 확장 함수
 * StationInfo(x, y) 데이터를 앱에서 사용하는 GeoPoint(lat, lng)로 변환합니다.
 * ODsay/Google 공통: x는 경도(lng), y는 위도(lat)입니다.
 */
fun StationInfo.toGeoPoint(): GeoPoint =
    GeoPoint(
        lat = y,
        lng = x
    )