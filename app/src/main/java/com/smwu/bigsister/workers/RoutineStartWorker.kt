package com.smwu.bigsister.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smwu.bigsister.utils.NotificationHelper

/**
 * 루틴 시작 시각에 맞춰 알림을 보내는 Worker
 */
class RoutineStartWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val routineId = inputData.getLong("routine_id", -1L)
        val title = inputData.getString("title") ?: "루틴 시작"
        val message = inputData.getString("message") ?: "이제 준비를 시작할 시간이에요!"

        NotificationHelper.showRoutineNotification(
            context = applicationContext,
            notificationId = (routineId.takeIf { it > 0 } ?: System.currentTimeMillis()).toInt(),
            title = title,
            message = message
        )

        return Result.success()
    }
}