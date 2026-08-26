package com.timeback.ui.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.timeback.ui.feature.apps.AppManagementScreen
import com.timeback.ui.feature.datamanagement.DataManagementScreen
import com.timeback.ui.feature.goals.GoalsScreen
import com.timeback.ui.feature.home.HomeScreen
import com.timeback.ui.feature.permission.PermissionScreen
import com.timeback.ui.feature.recovery.RecoveryScreen
import com.timeback.ui.feature.report.ReportScreen
import com.timeback.ui.feature.timeline.TimelineScreen

/**
 * Navigation Compose 라우팅
 * UI-01(Permission) → UI-02(Home) → 하위 화면들
 */
object Routes {
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val TIMELINE = "timeline"
    const val APPS = "apps"
    const val RECOVERY = "recovery"
    const val GOALS = "goals"
    const val REPORT = "report"
    const val DATA_MANAGEMENT = "data_management"
}

@Composable
fun TimeBackNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.PERMISSION) {

        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionReady = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateTimeline = { navController.navigate(Routes.TIMELINE) },
                onNavigateGoals = { navController.navigate(Routes.GOALS) },
                onNavigateReport = { navController.navigate(Routes.REPORT) }
            )
        }

        composable(Routes.TIMELINE) {
            TimelineScreen()
        }

        composable(Routes.APPS) {
            AppManagementScreen()
        }

        composable(Routes.RECOVERY) {
            RecoveryScreen(
                onNavigateToGoals = { navController.navigate(Routes.GOALS) }
            )
        }

        composable(Routes.GOALS) {
            GoalsScreen(
                onRecoveryForGoal = { navController.navigate(Routes.RECOVERY) }
            )
        }

        composable(Routes.REPORT) {
            ReportScreen()
        }

        composable(Routes.DATA_MANAGEMENT) {
            DataManagementScreen()
        }
    }
}
