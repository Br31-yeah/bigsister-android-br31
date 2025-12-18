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
import com.smwu.bigsister.ui.transit.TransitRouteScreen

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
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.Routine, BottomNavItem.Stats, BottomNavItem.Settings)

    Scaffold(
        bottomBar = {
            if (currentRoute != "onboarding" && !currentRoute.startsWith("live_mode")) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(
                        onNavigateToReservationAdd = { date -> navController.navigate("routine_reservation?date=$date") },
                        onNavigateToRoutineList = { navController.navigate("routine_list") },
                        onNavigateToStats = { navController.navigate("stats") },
                        onSettingsClick = { navController.navigate("settings") },
                        onNavigateToLiveMode = { id -> navController.navigate("live_mode/$id") }
                    )
                }

                composable("routine_builder?id={routineId}", arguments = listOf(navArgument("routineId") { type = NavType.StringType; nullable = true })) { entry ->
                    RoutineAddScreen(
                        routineId = entry.arguments?.getString("routineId")?.toLongOrNull(),
                        onNavigateBack = { navController.popBackStack() },
                        // ✅ 매개변수 개수 일치 (6개)
                        onNavigateToTransit = { fN, fL, tN, tL, time, mode ->
                            navController.navigate("transit_route?fromName=$fN&fromLatLng=$fL&toName=$tN&toLatLng=$tL&departureTime=$time&mode=$mode")
                        },
                        navController = navController
                    )
                }

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

                composable("live_mode/{routineId}", arguments = listOf(navArgument("routineId") { type = NavType.LongType })) {
                    LiveModeScreen(onFinishRoutine = { navController.popBackStack() })
                }

                composable("routine_list") { RoutineListScreen(onAddRoutineClick = { navController.navigate("routine_builder") }, onRoutineClick = { id -> navController.navigate("routine_builder?id=$id") }, onStartRoutineClick = { id -> navController.navigate("live_mode/$id") }, onSettingsClick = { navController.navigate("settings") }) }
                composable("stats") { StatsScreen() }
                composable("settings") { SettingsScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToLogin = onLogOut) }
                composable("onboarding") { OnboardingFlow(onComplete = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } }) }
            }
        }
    }
}