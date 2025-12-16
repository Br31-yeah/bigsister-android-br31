package com.smwu.bigsister.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smwu.bigsister.ui.auth.LoginScreen

/**
 * 앱 시작 시 진입점 역할을 하는 네비게이션
 * - 로그인 전 / 로그인 후 흐름만 구분한다
 */
@Composable
fun RootNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"   // ✅ 앱 첫 화면은 로그인
    ) {

        /* ---------------- 로그인 ---------------- */
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        // 로그인 화면으로 되돌아가지 않도록 제거
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- 메인 앱 ---------------- */
        composable("main") {
            AppNavigation()
        }
    }
}