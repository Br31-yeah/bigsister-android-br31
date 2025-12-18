package com.smwu.bigsister.data.model.transit

data class TransitRouteUiModel(
    /** UI 표시용 */
    val totalDurationText: String,

    /** 로직 / DB용 (분 단위) */
    val totalDurationMinutes: Long,

    val steps: List<TransitStepUiModel>,
    val encodedPolylines: List<String> = emptyList()
)