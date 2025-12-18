package com.smwu.bigsister.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smwu.bigsister.ui.auth.LoginScreen
import com.smwu.bigsister.ui.intro.OnboardingFlow
import com.smwu.bigsister.ui.viewModel.LoginViewModel
import com.smwu.bigsister.ui.viewModel.SettingsViewModel

// com/smwu/bigsister/ui/RootNavigation.kt

@Composable
fun RootNavigation(
    loginViewModel: LoginViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel() // ✅ 추가
) {
    val navController = rememberNavController()

    val currentUser by loginViewModel.currentUser.collectAsState()
    val hasSeenOnboarding by settingsViewModel.hasSeenOnboarding.collectAsState()

    // 💡 네비게이션 전략:
    // 1. 로그인이 안 되어 있으면 -> 'login'
    // 2. 로그인 되었는데 온보딩 안 봤으면 -> 'onboarding'
    // 3. 로그인 되었고 온보딩도 봤으면 -> 'main'
    val startDestination = if (currentUser == null) {
        "login"
    } else if (!hasSeenOnboarding) {
        "onboarding"
    } else {
        "main"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        /* ---------------- 1. 로그인 화면 ---------------- */
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 시 온보딩 여부에 따라 분기
                    val nextRoute = if (hasSeenOnboarding) "main" else "onboarding"
                    navController.navigate(nextRoute) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- 2. 온보딩 화면 ---------------- */
        composable("onboarding") {
            OnboardingFlow(
                onComplete = {
                    settingsViewModel.completeOnboarding() // ✅ 로컬에 완료 상태 저장
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        /* ---------------- 3. 메인 앱 화면 ---------------- */
        composable("main") {
            AppNavigation(
                onLogOut = {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}