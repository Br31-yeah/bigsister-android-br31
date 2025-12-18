package com.smwu.bigsister.data.repository

import com.smwu.bigsister.data.model.transit.GeoPoint
import com.smwu.bigsister.data.network.StationInfo

interface MapRepository {

    suspend fun getWalkingOrDrivingDuration(
        from: GeoPoint,
        to: GeoPoint,
        mode: String
    ): Long

    suspend fun getTransitDuration(
        from: GeoPoint,
        to: GeoPoint
    ): Long

    // ✅ 기존 searchStationByName에서 변경
    suspend fun searchPlacesByName(
        name: String
    ): List<StationInfo>
}