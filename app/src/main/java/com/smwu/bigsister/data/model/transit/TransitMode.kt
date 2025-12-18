package com.smwu.bigsister.data.model.transit

import androidx.compose.ui.graphics.Color
enum class TransitMode {
    WALK,
    BUS,
    SUBWAY,
    TRAIN
}
fun TransitMode.icon(): String = when (this) {
    TransitMode.WALK -> "🚶"
    TransitMode.BUS -> "🚌"
    TransitMode.SUBWAY -> "🚇"
    TransitMode.TRAIN -> "🚆"
}

fun TransitMode.backgroundColor(): Color = when (this) {
    TransitMode.WALK -> Color(0x33A9E8D4)     // mint
    TransitMode.BUS -> Color(0x336BCF7F)      // green
    TransitMode.SUBWAY -> Color(0x338B8FD9)   // lavender
    TransitMode.TRAIN -> Color(0x33FFB703)    // optional
}