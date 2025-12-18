package com.smwu.bigsister.data.model.transit

data class TransitStepUiModel(
    val mode: TransitMode,
    val description: String,
    val durationText: String,
    val encodedPolyline: String? = null // ✅ 추가
)