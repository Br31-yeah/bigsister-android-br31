package com.smwu.bigsister.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smwu.bigsister.ui.home.HomeScreen
import com.smwu.bigsister.ui.live.LiveModeScreen
import com.smwu.bigsister.ui.reservation.ReservationAddScreen
import com.smwu.bigsister.ui.routine.RoutineAddScreen
import com.smwu.bigsister.ui.routine.RoutineListScreen
import com.smwu.bigsister.ui.settings.SettingsScreen
import com.smwu.bigsister.ui.stats.StatsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToRoutineAdd = { date ->
                    navController.navigate("routine_reservation?date=$date")
                },
                onNavigateToRoutineList = { navController.navigate("routine_list") },
                onNavigateToSettings = { navController.navigate("settings") },
                // ✅ [추가] 'Live Mode'로 이동하는 함수 전달
                onNavigateToLiveMode = { routineId ->
                    navController.navigate("live_mode/$routineId")
                }
            )
        }

        // ... (ReservationAddScreen, RoutineListScreen, RoutineAddScreen ... )
        // (다른 composable들은 수정할 필요 없습니다)

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

        composable("stats") {
            StatsScreen()
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}