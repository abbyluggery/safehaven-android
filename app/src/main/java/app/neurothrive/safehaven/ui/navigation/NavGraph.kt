package app.neurothrive.safehaven.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.neurothrive.safehaven.ui.screens.*

/**
 * SafeHaven Navigation Routes
 */
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object ProfileSetup : Screen("profile_setup")
    object SilentCamera : Screen("silent_camera")
    object IncidentReport : Screen("incident_report")
    object EvidenceVault : Screen("evidence_vault")
    object DocumentVerification : Screen("document_verification")
    object ResourceFinder : Screen("resource_finder")
    object ResourceDetail : Screen("resource_detail/{resourceId}") {
        fun createRoute(resourceId: String) = "resource_detail/$resourceId"
    }
    object SafetyPlan : Screen("safety_plan")
    object Settings : Screen("settings")

    // Healthcare Journey Screens
    object HealthcareResourceFinder : Screen("healthcare_resources")
    object HealthcareJourneyPlanner : Screen("healthcare_journey_planner")
    object HealthcareJourneyDetail : Screen("healthcare_journey/{journeyId}") {
        fun createRoute(journeyId: String) = "healthcare_journey/$journeyId"
    }

    // Abuse Resources & SOS
    object AbuseResources : Screen("abuse_resources")
    object EmergencyContacts : Screen("emergency_contacts")

    // AI Risk Assessment
    object RiskAssessment : Screen("risk_assessment")
}

/**
 * SafeHaven Navigation Graph
 */
@Composable
fun SafeHavenNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Profile Setup
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Dashboard
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCamera = { navController.navigate(Screen.SilentCamera.route) },
                onNavigateToIncidents = { navController.navigate(Screen.IncidentReport.route) },
                onNavigateToEvidence = { navController.navigate(Screen.EvidenceVault.route) },
                onNavigateToVerification = { navController.navigate(Screen.DocumentVerification.route) },
                onNavigateToResources = { navController.navigate(Screen.ResourceFinder.route) },
                onNavigateToSafetyPlan = { navController.navigate(Screen.SafetyPlan.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHealthcare = { navController.navigate(Screen.HealthcareResourceFinder.route) },
                onNavigateToAbuseResources = { navController.navigate(Screen.AbuseResources.route) },
                onNavigateToEmergencyContacts = { navController.navigate(Screen.EmergencyContacts.route) },
                onNavigateToRiskAssessment = { navController.navigate(Screen.RiskAssessment.route) }
            )
        }

        // Silent Camera
        composable(Screen.SilentCamera.route) {
            SilentCameraScreen(
                onBack = { navController.popBackStack() },
                onPhotoSaved = { navController.popBackStack() }
            )
        }

        // Incident Report
        composable(Screen.IncidentReport.route) {
            IncidentReportScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Evidence Vault
        composable(Screen.EvidenceVault.route) {
            EvidenceVaultScreen(
                onBack = { navController.popBackStack() },
                onCaptureNew = { navController.navigate(Screen.SilentCamera.route) }
            )
        }

        // Document Verification
        composable(Screen.DocumentVerification.route) {
            DocumentVerificationScreen(
                onBack = { navController.popBackStack() },
                onVerified = { navController.popBackStack() }
            )
        }

        // Resource Finder
        composable(Screen.ResourceFinder.route) {
            ResourceFinderScreen(
                onBack = { navController.popBackStack() },
                onResourceClick = { resourceId ->
                    navController.navigate(Screen.ResourceDetail.createRoute(resourceId))
                }
            )
        }

        // Resource Detail
        composable(
            route = Screen.ResourceDetail.route,
            arguments = listOf(navArgument("resourceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resourceId = backStackEntry.arguments?.getString("resourceId") ?: ""
            ResourceDetailScreen(
                resourceId = resourceId,
                onBack = { navController.popBackStack() }
            )
        }

        // Safety Plan
        composable(Screen.SafetyPlan.route) {
            SafetyPlanScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Healthcare Resource Finder
        composable(Screen.HealthcareResourceFinder.route) {
            HealthcareResourceFinderScreen(
                onBack = { navController.popBackStack() },
                onClinicSelected = { clinic ->
                    // Navigate to journey planner with selected clinic
                    navController.navigate(Screen.HealthcareJourneyPlanner.route)
                }
            )
        }

        // Healthcare Journey Planner
        composable(Screen.HealthcareJourneyPlanner.route) {
            HealthcareJourneyPlannerScreen(
                selectedClinic = null, // TODO: Pass selected clinic
                onBack = { navController.popBackStack() },
                onSaveJourney = {
                    // Navigate back to home after saving
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        // Healthcare Journey Detail
        composable(
            route = Screen.HealthcareJourneyDetail.route,
            arguments = listOf(navArgument("journeyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val journeyId = backStackEntry.arguments?.getString("journeyId") ?: ""
            HealthcareJourneyDetailScreen(
                journeyId = journeyId,
                onBack = { navController.popBackStack() },
                onEditJourney = {
                    // TODO: Navigate to edit journey screen
                    navController.popBackStack()
                }
            )
        }

        // Abuse Resources & Education
        composable(Screen.AbuseResources.route) {
            AbuseResourcesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Emergency Contacts Settings
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // AI Risk Assessment
        composable(Screen.RiskAssessment.route) {
            RiskAssessmentScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSafetyPlan = { navController.navigate(Screen.SafetyPlan.route) },
                onNavigateToEmergencyContacts = { navController.navigate(Screen.EmergencyContacts.route) }
            )
        }
    }
}
