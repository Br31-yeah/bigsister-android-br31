package com.smwu.bigsister.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smwu.bigsister.ui.auth.LoginScreen
import com.smwu.bigsister.ui.viewModel.LoginViewModel

@Composable
fun RootNavigation(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    // 🔥 자동 로그인 체크
    // ViewModel에 있는 currentUser(FirebaseUser)를 감시합니다.
    val currentUser by viewModel.currentUser.collectAsState()

    // 유저 정보가 있으면 'main', 없으면 'login'에서 시작
    val startDestination = if (currentUser != null) "main" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        /* ---------------- 1. 로그인 화면 ---------------- */
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 메인으로 이동
                    navController.navigate("main") {
                        // 뒤로가기 눌러도 로그인 화면 안 나오게 스택 비우기
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- 2. 메인 앱 화면 ---------------- */
        composable("main") {
            // 방금 수정하신 AppNavigation을 여기서 호출합니다.
            AppNavigation(
                // ✅ 여기서 로그아웃 처리를 합니다!
                onLogOut = {
                    // 1. 로그인 화면으로 이동
                    navController.navigate("login") {
                        // 2. 백스택을 0(처음)까지 싹 비워서 뒤로가기 방지
                        popUpTo(0)
                    }
                }
            )
        }
    }
}