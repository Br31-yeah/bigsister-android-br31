package com.smwu.bigsister.data.network

import com.google.gson.annotations.SerializedName

data class GoogleDirectionsResponse(
    @SerializedName("routes")
    val routes: List<Route>
) {
    data class Route(
        @SerializedName("legs")
        val legs: List<Leg>
    )

    data class Leg(
        @SerializedName("duration")
        val duration: Duration
    )

    data class Duration(
        @SerializedName("value")
        val value: Long   // 초 단위
    )

    data class GoogleDistance(
        val value: Long // 미터
    )
}