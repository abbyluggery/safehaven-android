package app.neurothrive.safehaven.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.neurothrive.safehaven.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SilentCamera : Screen("camera")
    object IncidentReport : Screen("incident_report")
    object EvidenceVault : Screen("evidence_vault")
    object Resources : Screen("resources")
    object Settings : Screen("settings")
}

@Composable
fun SafeHavenNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.SilentCamera.route) {
            SilentCameraScreen(navController = navController)
        }

        composable(Screen.IncidentReport.route) {
            IncidentReportScreen(navController = navController)
        }

        composable(Screen.EvidenceVault.route) {
            EvidenceVaultScreen(navController = navController)
        }

        composable(Screen.Resources.route) {
            ResourcesScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
