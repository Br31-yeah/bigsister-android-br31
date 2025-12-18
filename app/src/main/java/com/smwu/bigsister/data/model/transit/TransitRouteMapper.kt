package com.smwu.bigsister.data.model.transit

import com.smwu.bigsister.data.network.Route
import com.smwu.bigsister.data.network.RouteStep

fun Route.toUiModel(): TransitRouteUiModel {
    val firstLeg = legs.firstOrNull()
    val steps: List<TransitStepUiModel> = firstLeg?.steps?.mapNotNull { it.toUiStep() } ?: emptyList()

    // ✅ "1시간 24분" 텍스트 대신 "5040s" 데이터에서 's'만 떼고 숫자로 변환
    val rawSeconds = firstLeg?.staticDuration?.removeSuffix("s")?.toLongOrNull() ?: 0L
    val totalMinutes = rawSeconds / 60

    return TransitRouteUiModel(
        totalDurationText = localizedValues?.duration?.text ?: "${totalMinutes}분",
        totalDurationMinutes = totalMinutes, // ✅ 이제 11분이 아닌 정확한 시간(예: 84분)이 저장됩니다.
        steps = steps,
        encodedPolylines = steps.mapNotNull { it.encodedPolyline }
    )
}

fun RouteStep.toUiStep(): TransitStepUiModel? {
    val mode = when (travelMode) {
        "WALKING" -> TransitMode.WALK
        "TRANSIT" -> when (transitDetails?.transitLine?.vehicle?.type) {
            "BUS" -> TransitMode.BUS
            "SUBWAY" -> TransitMode.SUBWAY
            "TRAIN" -> TransitMode.TRAIN
            else -> TransitMode.BUS
        }
        else -> return null
    }

    val description = when (mode) {
        TransitMode.WALK -> "도보 이동"
        else -> transitDetails?.transitLine?.nameShort?.let { "${if (mode == TransitMode.BUS) "버스" else "지하철"} $it" } ?: "대중교통"
    }

    val stepSeconds = staticDuration?.removeSuffix("s")?.toLongOrNull() ?: 0L

    return TransitStepUiModel(
        mode = mode,
        description = description,
        durationText = "${stepSeconds / 60}분",
        encodedPolyline = polyline?.encodedPolyline
    )
}