package com.smwu.bigsister.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smwu.bigsister.MainActivity
import com.smwu.bigsister.R

/**
 * 알림 채널 생성 + 공통 알림 헬퍼
 */
object NotificationHelper {

    private const val CHANNEL_ROUTINE = "routine_channel"

    // Application.onCreate()에서 한 번만 호출
    fun createNotificationChannels(context: Context) {
        // 🔥 API 26 이하에서는 NotificationChannel 자체가 없음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val routineChannel = NotificationChannel(
                CHANNEL_ROUTINE,
                "루틴 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "루틴 시작/출발/종료 알림 채널"
            }

            manager.createNotificationChannel(routineChannel)
        }
    }

    // 루틴 관련 알림 보여주기
    fun showRoutineNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ROUTINE)
            .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 없으면 여기서 크래시 나니 일단 기본 런처 사용
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}