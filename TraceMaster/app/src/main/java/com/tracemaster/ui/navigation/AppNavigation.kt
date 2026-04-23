package com.tracemaster.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tracemaster.ui.screens.home.HomeScreen
import com.tracemaster.ui.screens.map.MapScreen
import com.tracemaster.ui.screens.record.RecordScreen
import com.tracemaster.ui.screens.list.TrackListScreen
import com.tracemaster.ui.screens.detail.TrackDetailScreen
import com.tracemaster.ui.screens.settings.SettingsScreen
import com.tracemaster.ui.screens.importexport.ImportExportScreen

/**
 * 应用导航路由
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Map : Screen("map")
    object Record : Screen("record")
    object TrackList : Screen("track_list")
    object TrackDetail : Screen("track_detail/{trackId}") {
        fun createRoute(trackId: Long) = "track_detail/$trackId"
    }
    object Settings : Screen("settings")
    object ImportExport : Screen("import_export")
}

/**
 * 应用主导航
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToList = { navController.navigate(Screen.TrackList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartRecording = { navController.navigate(Screen.Record.route) }
            )
        }
        
        composable(Screen.Record.route) {
            RecordScreen(
                onNavigateBack = { navController.popBackStack() },
                onFinishRecording = { trackId ->
                    navController.popBackStack()
                    navController.navigate(Screen.TrackDetail.createRoute(trackId))
                }
            )
        }
        
        composable(Screen.TrackList.route) {
            TrackListScreen(
                onNavigateBack = { navController.popBackStack() },
                onTrackClick = { trackId ->
                    navController.navigate(Screen.TrackDetail.createRoute(trackId))
                }
            )
        }
        
        composable(
            route = Screen.TrackDetail.route,
            arguments = listOf(navArgument("trackId") { type = NavType.LongType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getLong("trackId") ?: return@composable
            TrackDetailScreen(
                trackId = trackId,
                onNavigateBack = { navController.popBackStack() },
                onShareClick = { /* TODO: 分享功能 */ }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.ImportExport.route) {
            ImportExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
