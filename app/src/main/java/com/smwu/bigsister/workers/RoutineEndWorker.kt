package com.smwu.bigsister.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smwu.bigsister.utils.NotificationHelper

/**
 * 루틴 종료 시각에 맞춰 기록을 유도하는 Worker
 */
class RoutineEndWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val routineId = inputData.getLong("routine_id", -1L)
        val title = inputData.getString("title") ?: "루틴이 끝났어요"
        val message = inputData.getString("message") ?: "오늘 루틴을 잘 지켰는지 기록해 볼까요?"

        NotificationHelper.showRoutineNotification(
            context = applicationContext,
            notificationId = ((routineId.takeIf { it > 0 } ?: System.currentTimeMillis()) + 2000).toInt(),
            title = title,
            message = message
        )

        return Result.success()
    }
}