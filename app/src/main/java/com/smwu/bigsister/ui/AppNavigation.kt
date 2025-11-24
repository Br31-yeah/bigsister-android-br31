package com.smwu.bigsister.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smwu.bigsister.ui.home.HomeScreen
import com.smwu.bigsister.ui.intro.OnboardingFlow
import com.smwu.bigsister.ui.live.LiveModeScreen
import com.smwu.bigsister.ui.reservation.ReservationAddScreen
import com.smwu.bigsister.ui.routine.RoutineAddScreen
import com.smwu.bigsister.ui.routine.RoutineListScreen
import com.smwu.bigsister.ui.settings.SettingsScreen
import com.smwu.bigsister.ui.stats.StatsScreen

// 하단 탭 메뉴 정의
sealed class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : BottomNavItem("home", "홈", Icons.Default.Home)
    object Routine : BottomNavItem("routine_list", "루틴", Icons.Default.List)
    object Live : BottomNavItem("live_mode_entry", "실행", Icons.Default.PlayArrow)
    object Stats : BottomNavItem("stats", "통계", Icons.Default.DateRange)
    object Settings : BottomNavItem("settings", "설정", Icons.Default.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 하단 탭에 보여줄 목록
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Routine,
        // BottomNavItem.Live,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // 하단 바를 보여줄 화면들 (온보딩이나 루틴 추가 화면에서는 숨김)
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF8B8FD9),
                                selectedTextColor = Color(0xFF8B8FD9),
                                indicatorColor = Color(0xFFE3E4FA)
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "onboarding" // 시작점: 온보딩
            ) {
                // 1. 온보딩 (시작 -> 타입선택 -> 알림설정)
                composable("onboarding") {
                    // ✅ [수정] OnboardingFlow를 연결하여 전체 과정을 진행
                    OnboardingFlow(
                        onComplete = {
                            // 모든 설정이 끝나면 홈으로 이동하고, 뒤로가기 막음 (앱 종료됨)
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // 2. 홈
                composable("home") {
                    HomeScreen(
                        onNavigateToRoutineAdd = { date ->
                            navController.navigate("routine_reservation?date=$date")
                        },
                        onNavigateToRoutineList = { navController.navigate("routine_list") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToLiveMode = { routineId ->
                            navController.navigate("live_mode/$routineId")
                        }
                    )
                }

                // 3. 루틴 목록
                composable("routine_list") {
                    RoutineListScreen(
                        onAddRoutineClick = { navController.navigate("routine_builder") },
                        onRoutineClick = { routineId ->
                            navController.navigate("routine_builder?id=$routineId")
                        },
                        onStartRoutineClick = { routineId ->
                            navController.navigate("live_mode/$routineId")
                        }
                    )
                }

                // 4. 루틴 생성/수정
                composable(
                    route = "routine_builder?id={routineId}",
                    arguments = listOf(navArgument("routineId") {
                        type = NavType.IntType
                        defaultValue = -1
                    })
                ) { backStackEntry ->
                    val routineId = backStackEntry.arguments?.getInt("routineId")
                    RoutineAddScreen(
                        routineId = if (routineId == -1) null else routineId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 5. 예약 추가
                composable(
                    route = "routine_reservation?date={date}",
                    arguments = listOf(navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                    })
                ) { backStackEntry ->
                    ReservationAddScreen(
                        dateString = backStackEntry.arguments?.getString("date"),
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoutineAdd = { navController.navigate("routine_builder") }
                    )
                }

                // 6. 실행 모드
                composable(
                    route = "live_mode/{routineId}",
                    arguments = listOf(navArgument("routineId") {
                        type = NavType.IntType
                    })
                ) {
                    LiveModeScreen(
                        onFinishRoutine = { navController.popBackStack() }
                    )
                }

                // (탭용) 실행 모드 진입점
                composable("live_mode_entry") {
                    Text("루틴 탭에서 실행할 루틴을 선택해주세요.")
                }

                // 7. 통계
                composable("stats") {
                    StatsScreen()
                }

                // 8. 설정
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}