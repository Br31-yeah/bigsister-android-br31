package com.smwu.bigsister.utils

import com.smwu.bigsister.data.local.StepEntity

/**
 * 루틴 시작 시각 + Step 리스트로
 * 출발 시각 / 종료 시각 계산
 *
 * 시간 계산 규칙:
 * - calculatedDuration 이 있으면 우선 사용
 * - 없으면 baseDuration 사용
 */
object StepTimeCalculator {

    /**
     * 이동 Step 출발 시각 계산
     *
     * 루틴 시작 시각 +
     * 이동 Step 이전 모든 Step 소요 시간 합
     */
    fun calculateDepartureTimeMillis(
        routineStartMillis: Long,
        steps: List<StepEntity>
    ): Long {
        val moveIndex = steps.indexOfFirst { it.isTransport }
        if (moveIndex == -1) return routineStartMillis

        val minutesBeforeMove =
            steps.take(moveIndex).sumOf { step ->
                step.calculatedDuration ?: step.baseDuration
            }

        return routineStartMillis + minutesBeforeMove * 60_000L
    }

    /**
     * 루틴 종료 시각 계산
     *
     * 루틴 시작 시각 +
     * 전체 Step 소요 시간 합
     */
    fun calculateEndTimeMillis(
        routineStartMillis: Long,
        steps: List<StepEntity>
    ): Long {
        val totalMinutes =
            steps.sumOf { step ->
                step.calculatedDuration ?: step.baseDuration
            }

        return routineStartMillis + totalMinutes * 60_000L
    }
}