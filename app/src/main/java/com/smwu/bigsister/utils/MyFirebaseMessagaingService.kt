package com.smwu.bigsister.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.smwu.bigsister.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 나중에 서버에 토큰 보내고 싶으면 여기에서 처리
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "빅시스터 알림"
        val body = message.notification?.body ?: "알림입니다."

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "bigsister_default_channel"
        val notificationId = 1

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 생성 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "일반 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "빅시스터 기본 알림 채널"
            }
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            // 🔴 여기! 존재하는 아이콘으로 바꿈. 필요하면 나중에 직접 만든 아이콘으로 교체
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)   // setContentTitle는 Builder의 메서드
            .setContentText(body)
            .setAutoCancel(true)

        // Android 13+ 알림 권한 체크
        val canNotify =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            manager.notify(notificationId, builder.build())
        }
        // 권한이 없으면 그냥 조용히 무시 (필요하면 로그 추가 가능)
    }
}