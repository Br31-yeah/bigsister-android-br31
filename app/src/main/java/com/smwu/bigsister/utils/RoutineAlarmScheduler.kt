package com.smwu.bigsister.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.smwu.bigsister.data.local.StepEntity

object RoutineAlarmScheduler {

    fun scheduleAll(
        context: Context,
        routineId: Long,
        routineStartMillis: Long,
        steps: List<StepEntity>
    ) {
        var currentTime = routineStartMillis

        for (step in steps) {
            val duration = step.calculatedDuration ?: step.baseDuration

            scheduleStepAlarm(
                context = context,
                timeMillis = currentTime,
                title = "⏰ ${step.name}",
                message = "${duration}분 동안 진행할 차례예요!"
            )

            currentTime += duration * 60_000L
        }
    }

    private fun scheduleStepAlarm(
        context: Context,
        timeMillis: Long,
        title: String,
        message: String
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (timeMillis % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeMillis,
            pendingIntent
        )
    }

    /**
     * 🔹 예약 삭제 시 호출
     * 현재는 stub (컴파일 + 안정성 목적)
     */
    fun cancelAllForRoutine(
        context: Context,
        routineId: Long
    ) {
        // TODO: routineId 기준 알람 cancel 전략 구현
    }
}