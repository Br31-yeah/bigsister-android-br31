package com.smwu.bigsister.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.reservation.ReservationAddScreen
import com.smwu.bigsister.ui.routine.RoutineAddScreen
import com.smwu.bigsister.ui.routine.RoutineListScreen
import com.smwu.bigsister.ui.settings.SettingsScreen
import com.smwu.bigsister.ui.stats.StatsScreen
import com.smwu.bigsister.ui.transit.TransitRouteScreen
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

/* ------------------------------------------------------------
   Bottom Navigation Items (2번 브랜치 스타일 유지)
------------------------------------------------------------ */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "홈", Icons.Default.Home)
    object Routine : BottomNavItem("routine_list", "루틴", Icons.AutoMirrored.Filled.List)
    object Stats : BottomNavItem("stats", "통계", Icons.Default.DateRange)
    object Settings : BottomNavItem("settings", "설정", Icons.Default.Settings)
}

@Composable
fun AppNavigation(onLogOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // ✅ 에러 방지: null일 경우 빈 문자열로 처리하여 startsWith 호출 시 안전성 확보
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Routine,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    // ✅ 바텀바 표시 로직 (2번 브랜치의 화이트리스트 방식 + 1번의 라이브모드 예외처리 통합)
    val showBottomBar = bottomNavItems.any { item -> currentRoute.startsWith(item.route) }
            && !currentRoute.startsWith("live_mode")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            // ✅ 2번 브랜치의 테마 컬러 적용
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF8B8FD9),
                                selectedTextColor = Color(0xFF8B8FD9),
                                indicatorColor = Color(0xFFE3E4FA)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "home") {

                /* 1. 온보딩 (2번) */
                composable("onboarding") {
                    OnboardingFlow(onComplete = {
                        navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                    })
                }

                /* 2. 홈 화면 (통합) */
                composable("home") {
                    HomeScreen(
                        onNavigateToReservationAdd = { date -> navController.navigate("routine_reservation?date=$date") },
                        onNavigateToRoutineList = { navController.navigate("routine_list") },
                        onNavigateToStats = { navController.navigate("stats") },
                        onSettingsClick = { navController.navigate("settings") },
                        onNavigateToLiveMode = { id -> navController.navigate("live_mode/$id") }
                    )
                }

                /* 3. 루틴 목록 (2번 UI + 1번 경로) */
                composable("routine_list") {
                    RoutineListScreen(
                        onAddRoutineClick = { navController.navigate("routine_builder") },
                        onRoutineClick = { id -> navController.navigate("routine_builder?id=$id") },
                        onStartRoutineClick = { id -> navController.navigate("live_mode/$id") },
                        onSettingsClick = { navController.navigate("settings") }
                    )
                }

                /* 4. 루틴 빌더 (1번 브랜치의 핵심 로직: Transit 연결 기능 유지) */
                composable(
                    route = "routine_builder?id={routineId}",
                    arguments = listOf(navArgument("routineId") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) { entry ->
                    RoutineAddScreen(
                        routineId = entry.arguments?.getString("routineId")?.toLongOrNull(),
                        onNavigateBack = { navController.popBackStack() },
                        // ✅ 1번 브랜치의 6개 인자 콜백 유지 (중요!)
                        onNavigateToTransit = { fN, fL, tN, tL, time, mode ->
                            navController.navigate("transit_route?fromName=$fN&fromLatLng=$fL&toName=$tN&toLatLng=$tL&departureTime=$time&mode=$mode")
                        },
                        navController = navController
                    )
                }

                /* 5. 대중교통 경로 선택 (1번 브랜치 전용 페이지) */
                composable(
                    route = "transit_route?fromName={fromName}&fromLatLng={fromLatLng}&toName={toName}&toLatLng={toLatLng}&departureTime={departureTime}&mode={mode}",
                    arguments = listOf(
                        navArgument("fromName") { type = NavType.StringType },
                        navArgument("fromLatLng") { type = NavType.StringType },
                        navArgument("toName") { type = NavType.StringType },
                        navArgument("toLatLng") { type = NavType.StringType },
                        navArgument("departureTime") { type = NavType.StringType },
                        navArgument("mode") { type = NavType.StringType; defaultValue = "TRANSIT" }
                    )
                ) { entry ->
                    TransitRouteScreen(
                        fromName = entry.arguments?.getString("fromName") ?: "",
                        fromLatLng = entry.arguments?.getString("fromLatLng") ?: "",
                        toName = entry.arguments?.getString("toName") ?: "",
                        toLatLng = entry.arguments?.getString("toLatLng") ?: "",
                        departureTime = entry.arguments?.getString("departureTime") ?: "09:00",
                        onBack = { navController.popBackStack() },
                        onRouteSelected = { draft ->
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_transit_draft", draft)
                            navController.popBackStack()
                        }
                    )
                }

                /* 6. 별도 장소 검색 페이지 (2번 브랜치에서 추가된 경로) */
                composable(
                    route = "station_search?type={type}",
                    arguments = listOf(navArgument("type") { type = NavType.StringType; defaultValue = "origin" })
                ) {
                    StationSearchScreen(
                        viewModel = hiltViewModel(),
                        onDismiss = { navController.popBackStack() },
                        onStationSelected = { /* 필요 시 구현 */ navController.popBackStack() }
                    )
                }

                /* 7. 예약 추가 (2번 기반) */
                composable(
                    route = "routine_reservation?date={date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType; nullable = true })
                ) { entry ->
                    ReservationAddScreen(
                        dateString = entry.arguments?.getString("date"),
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoutineAdd = { navController.navigate("routine_builder") }
                    )
                }

                /* 8. 라이브 모드 (1번의 파라미터 + 2번의 타입 안정성) */
                composable(
                    route = "live_mode/{routineId}",
                    arguments = listOf(navArgument("routineId") { type = NavType.LongType })
                ) {
                    LiveModeScreen(onFinishRoutine = { navController.popBackStack() })
                }

                /* 9. 통계 및 설정 (공통) */
                composable("stats") { StatsScreen() }
                composable("settings") { SettingsScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToLogin = onLogOut) }
            }
        }
    }
}