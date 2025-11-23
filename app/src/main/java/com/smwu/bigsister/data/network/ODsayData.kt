package com.smwu.bigsister.data.network

// 1. 전체 응답 껍데기
data class ODsayResponse(
    val result: ODsayResult?
)

// 2. 결과 내용물
data class ODsayResult(
    val path: List<ODsayPath>? // 찾은 경로 리스트
)

// 3. 하나의 경로 (예: 지하철로 가는 방법 1)
data class ODsayPath(
    val info: ODsayInfo,       // 요약 정보 (소요시간, 요금 등)
    val subPath: List<ODsaySubPath>? // 상세 이동 구간 (걷기 -> 지하철 -> 걷기)
)

// 4. 경로 요약 정보
data class ODsayInfo(
    val totalTime: Int,     // 총 소요시간 (분)
    val payment: Int,       // 요금 (원)
    val firstStartStation: String, // 출발역
    val lastEndStation: String     // 도착역
)

// 5. 상세 이동 구간 (이게 중요! 지도에 그릴 좌표가 여기 있음)
data class ODsaySubPath(
    val trafficType: Int,   // 1:지하철, 2:버스, 3:걷기
    val sectionTime: Int,   // 이 구간 소요시간
    val stationCount: Int?, // 거쳐가는 정거장 수
    val startName: String?, // 출발지 이름
    val endName: String?,   // 도착지 이름
    val lane: List<ODsayLane>? // 몇 호선인지 정보
)

data class ODsayLane(
    val name: String, // "1호선", "2호선" 등
    val subwayCode: Int // 노선 색상 구분용 코드
)

// ▼▼▼ [추가] 지하철역 검색 결과용 데이터 클래스 ▼▼▼
data class ODsayStationResponse(
    val result: ODsayStationResult?
)

data class ODsayStationResult(
    val station: List<StationInfo>?
)

data class StationInfo(
    val stationName: String, // 역 이름 (예: 강남)
    val stationID: Int,      // 역 ID
    val x: Double,           // 경도 (Longitude)
    val y: Double,           // 위도 (Latitude)
    val laneName: String     // 노선 이름 (예: 서울 2호선)
)