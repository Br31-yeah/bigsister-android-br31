package com.smwu.bigsister.utils

import com.smwu.bigsister.data.local.StepEntity

/**
 * 루틴 시작 시각 + Step 리스트로
 * 출발 시각 / 종료 시각 계산
 */
object StepTimeCalculator {

    // 이동 Step 전에 있는 준비 Step duration 합산해서 출발 시각 계산
    fun calculateDepartureTimeMillis(
        routineStartMillis: Long,
        steps: List<StepEntity>
    ): Long {
        val moveIndex = steps.indexOfFirst { it.isTransport }
        if (moveIndex == -1) return routineStartMillis // 이동 스텝 없으면 그냥 시작 시각

        val minutesBeforeMove = steps.take(moveIndex).sumOf { it.duration }
        return routineStartMillis + minutesBeforeMove * 60_000L
    }

    // 전체 duration 합산해서 루틴 종료 시각 계산
    fun calculateEndTimeMillis(
        routineStartMillis: Long,
        steps: List<StepEntity>
    ): Long {
        val totalMinutes = steps.sumOf { it.duration }
        return routineStartMillis + totalMinutes * 60_000L
    }
}