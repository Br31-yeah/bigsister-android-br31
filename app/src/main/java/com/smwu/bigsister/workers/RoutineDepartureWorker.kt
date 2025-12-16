package com.smwu.bigsister.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smwu.bigsister.utils.NotificationHelper

/**
 * 출발해야 하는 시각에 맞춰 알림을 보내는 Worker
 */
class RoutineDepartureWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val routineId = inputData.getLong("routine_id", -1L)
        val title = inputData.getString("title") ?: "출발할 시간이에요"
        val message = inputData.getString("message") ?: "지금 나가야 지각하지 않아요!"

        NotificationHelper.showRoutineNotification(
            context = applicationContext,
            notificationId = ((routineId.takeIf { it > 0 } ?: System.currentTimeMillis()) + 1000).toInt(),
            title = title,
            message = message
        )

        return Result.success()
    }
}