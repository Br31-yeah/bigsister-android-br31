package com.smwu.bigsister.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.reservation.ReservationAddScreen
import com.smwu.bigsister.ui.routine.RoutineAddScreen
import com.smwu.bigsister.ui.routine.RoutineListScreen
import com.smwu.bigsister.ui.settings.SettingsScreen
import com.smwu.bigsister.ui.stats.StatsScreen

/* ------------------------------------------------------------
   Bottom Navigation Items
------------------------------------------------------------ */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : BottomNavItem("home", "홈", Icons.Default.Home)
    // ✅ [수정] Warning 해결: Icons.Default.List -> Icons.AutoMirrored.Filled.List
    object Routine : BottomNavItem("routine_list", "루틴", Icons.AutoMirrored.Filled.List)
    object Stats : BottomNavItem("stats", "통계", Icons.Default.DateRange)
    object Settings : BottomNavItem("settings", "설정", Icons.Default.Settings)
}

/* ------------------------------------------------------------
   App Navigation
------------------------------------------------------------ */
@Composable
fun AppNavigation(
    // ✅ [추가] RootNavigation에서 전달하는 로그아웃 기능을 받기 위한 파라미터
    onLogOut: () -> Unit
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Routine,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = bottomNavItems.any { item ->
        currentRoute?.startsWith(item.route) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        val selected =
                            navBackStackEntry?.destination?.hierarchy
                                ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
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

            NavHost(
                navController = navController,
                startDestination = "home" // 로그인 후 진입이므로 home 시작
            ) {
                // Onboarding은 RootNavigation으로 이동했으므로 여기서 제거해도 되지만,
                // 혹시 내부 이동용으로 남겨둔다면 유지.
                composable("onboarding") {
                    OnboardingFlow(
                        onComplete = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                /* ------------------ Home ------------------ */
                composable("home") {
                    HomeScreen(
                        onNavigateToReservationAdd = { date ->
                            navController.navigate("routine_reservation?date=$date")
                        },
                        onNavigateToRoutineList = {
                            navController.navigate("routine_list")
                        },
                        onNavigateToStats = {
                            navController.navigate("stats")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }

                /* ------------------ Routine List ------------------ */
                composable("routine_list") {
                    RoutineListScreen(
                        onAddRoutineClick = {
                            navController.navigate("routine_builder")
                        },
                        onRoutineClick = { id ->
                            navController.navigate("routine_builder?id=$id")
                        },
                        onStartRoutineClick = { id ->
                            navController.navigate("live_mode/$id")
                        },
                        onSettingsClick = {
                            navController.navigate("settings")
                        }
                    )
                }

                /* ------------------ Routine Add / Edit ------------------ */
                composable(
                    route = "routine_builder?id={routineId}",
                    arguments = listOf(
                        navArgument("routineId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { entry ->
                    val routineId =
                        entry.arguments?.getString("routineId")?.toLongOrNull()

                    RoutineAddScreen(
                        routineId = routineId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                /* ------------------ Station Search ------------------ */
                composable(
                    route = "station_search?type={type}",
                    arguments = listOf(
                        navArgument("type") {
                            type = NavType.StringType
                            defaultValue = "origin"
                        }
                    )
                ) {
                    StationSearchScreen(
                        onDismiss = {
                            navController.popBackStack()
                        },
                        onStationSelected = { station ->
                            navController.popBackStack()
                        }
                    )
                }

                /* ------------------ Reservation Add ------------------ */
                composable(
                    route = "routine_reservation?date={date}",
                    arguments = listOf(
                        navArgument("date") {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) { entry ->
                    ReservationAddScreen(
                        dateString = entry.arguments?.getString("date"),
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToRoutineAdd = {
                            navController.navigate("routine_builder")
                        }
                    )
                }

                /* ------------------ Live Mode ------------------ */
                composable(
                    route = "live_mode/{routineId}",
                    arguments = listOf(
                        navArgument("routineId") { type = NavType.IntType }
                    )
                ) {
                    LiveModeScreen(
                        onFinishRoutine = {
                            navController.popBackStack()
                        }
                    )
                }

                /* ------------------ Stats ------------------ */
                composable("stats") {
                    StatsScreen()
                }

                /* ------------------ Settings ------------------ */
                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        // ✅ [수정] 외부에서 받은 로그아웃 함수 연결
                        onNavigateToLogin = onLogOut
                    )
                }
            }
        }
    }
}