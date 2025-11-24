package com.smwu.bigsister.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.smwu.bigsister.ui.intro.SisterTypeScreen // 👈 아까 만든 온보딩 화면 임포트
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
    object Live : BottomNavItem("live_mode_entry", "실행", Icons.Default.PlayArrow) // 실행 대기 화면용
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
        // BottomNavItem.Live, // '실행' 탭은 보통 루틴 선택 후 진입하므로 탭에서 뺄 수도 있지만, React 구조 따라 넣음
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    // 현재 보고 있는 화면이 어디인지 확인
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // 하단 바를 보여줄 화면들 지정 (온보딩이나 루틴 추가 화면에서는 숨김)
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
                                selectedIconColor = Color(0xFF8B8FD9), // 피그마의 보라색
                                selectedTextColor = Color(0xFF8B8FD9),
                                indicatorColor = Color(0xFFE3E4FA)
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    // 탭 클릭 시 스택 관리 (Back 버튼 누르면 홈으로 오게)
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
        // Scaffold의 padding을 적용하기 위해 Box로 감쌉니다.
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = "onboarding" // 👈 시작을 '온보딩'으로 변경 (React 로직 반영)
            ) {
                // 1. 온보딩 (언니 타입 선택) - React의 <Onboarding />
                composable("onboarding") {
                    SisterTypeScreen(
                        onNextClick = { selectedType ->
                            // 타입 선택 후 홈으로 이동
                            // 실제로는 여기서 DataStore에 selectedType을 저장해야 함
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true } // 뒤로가기 못하게
                            }
                        }
                    )
                }

                // 2. 홈 - React의 <Home />
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

                // 3. 루틴 목록 - React의 <RoutineList />
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

                // 4. 루틴 생성/수정 - React의 <RoutineBuilder />
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

                // 5. 예약 추가 (기존 유지)
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

                // 6. 실행 모드 - React의 <LiveMode />
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

                // (탭용) 실행 모드 진입점 임시 처리
                composable("live_mode_entry") {
                    // 실제로는 실행할 루틴을 선택해야 하므로, 일단 루틴 리스트로 보내거나
                    // 최근 루틴을 실행하는 로직이 필요합니다. 여기선 임시로 텍스트 표시.
                    Text("루틴 탭에서 실행할 루틴을 선택해주세요.")
                }

                // 7. 통계 - React의 <Stats />
                composable("stats") {
                    StatsScreen()
                }

                // 8. 설정 - React의 <Settings />
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}