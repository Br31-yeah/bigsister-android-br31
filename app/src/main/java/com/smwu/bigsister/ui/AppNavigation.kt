package com.smwu.bigsister.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
    object Routine : BottomNavItem("routine_list", "루틴", Icons.Default.List)
    object Stats : BottomNavItem("stats", "통계", Icons.Default.DateRange)
    object Settings : BottomNavItem("settings", "설정", Icons.Default.Settings)
}

/* ------------------------------------------------------------
   App Navigation
------------------------------------------------------------ */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Routine,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // BottomBar 표시 여부
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
                startDestination = "onboarding"
            ) {

                /* ------------------ Onboarding ------------------ */
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

                /* ------------------ Station Search (출발지/도착지) ------------------ */
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
                            // TODO: RoutineViewModel에 선택된 역 반영
                            // viewModel.setOriginStation(station) / setDestinationStation(station)

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
                        }
                    )
                }
            }
        }
    }
}