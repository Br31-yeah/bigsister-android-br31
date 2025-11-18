package com.smwu.bigsister.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest

/**
 * 카카오 개발자 사이트에 등록할 '키 해시'를 Logcat에 출력하는 유틸리티
 */
fun getAppKeyHash(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 9 (API 28) 이상
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signatures = packageInfo.signingInfo.apkContentsSigners
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "Key Hash: $hash") // "Key Hash:" 로그 출력
            }
        } else {
            // Android 8 (API 27) 이하
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in packageInfo.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hash = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d("KeyHash", "Key Hash: $hash") // "Key Hash:" 로그 출력
            }
        }
    } catch (e: Exception) {
        Log.e("KeyHash", "Error getting key hash", e)
    }
}