package com.repforge.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.repforge.feature.auth.LoginScreen
import com.repforge.feature.auth.SignUpScreen
import com.repforge.feature.onboarding.OnboardingScreen
import com.repforge.feature.routines.ExerciseDictionaryScreen
import com.repforge.feature.routines.RapidTemplateWizardScreen
import com.repforge.feature.routines.RoutineDetailScreen
import com.repforge.feature.routines.RoutineListScreen

/**
 * Main navigation host for RepForge.
 * Includes onboarding, authentication, and the 5-tab main experience.
 */
@Composable
fun RepForgeNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String = TopLevelDestination.TODAY.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.padding(innerPadding)
    ) {
        // First-Run / Auth Flows
        composable("onboarding") {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(TopLevelDestination.TODAY.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = {
                    navController.navigate(TopLevelDestination.TODAY.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(TopLevelDestination.TODAY.route) {
                        popUpTo("signup") { inclusive = true }
                    }
                }
            )
        }

        // Tab 1: Today / Active Session
        composable(TopLevelDestination.TODAY.route) {
            com.repforge.feature.session.ActiveSessionScreen(
                onFinishWorkout = { sessionId ->
                    navController.navigate("workout_summary/$sessionId")
                }
            )
        }

        composable(
            route = "active_session/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            com.repforge.feature.session.ActiveSessionScreen(
                onFinishWorkout = { sessionId ->
                    navController.navigate("workout_summary/$sessionId")
                }
            )
        }

        composable(
            route = "workout_summary/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) {
            com.repforge.feature.session.WorkoutSummaryScreen(
                onDoneClick = {
                    navController.navigate(TopLevelDestination.TODAY.route) {
                        popUpTo(TopLevelDestination.TODAY.route) { inclusive = true }
                    }
                }
            )
        }

        // Tab 2: Routines & Discovery
        composable(TopLevelDestination.ROUTINES.route) {
            RoutineListScreen(
                onCreateRoutineClick = { navController.navigate("rapid_template_wizard") },
                onRoutineClick = { routineId -> navController.navigate("routine_detail/$routineId") },
                onStartWorkoutClick = { routineId ->
                    navController.navigate("active_session/$routineId")
                },
                onOpenLibraryClick = { navController.navigate("exercise_dictionary") },
                onAIIngestClick = { navController.navigate("ai_ingestion") }
            )
        }

        composable("rapid_template_wizard") {
            RapidTemplateWizardScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoutineSaved = { routineId ->
                    navController.navigate("routine_detail/$routineId") {
                        popUpTo("rapid_template_wizard") { inclusive = true }
                    }
                },
                onStartWorkout = { routineId ->
                    navController.navigate("active_session/$routineId") {
                        popUpTo("rapid_template_wizard") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "routine_detail/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            RoutineDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { routineId ->
                    navController.navigate("active_session/$routineId")
                }
            )
        }

        composable("exercise_dictionary") {
            ExerciseDictionaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("ai_ingestion") {
            com.repforge.feature.ingestion.AIIngestionScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoutineImported = { routineId ->
                    navController.navigate("routine_detail/$routineId") {
                        popUpTo("ai_ingestion") { inclusive = true }
                    }
                }
            )
        }

        // Tab 3: Heatmap & Analytics
        composable(TopLevelDestination.HEATMAP.route) {
            com.repforge.feature.heatmap.HeatmapScreen()
        }

        // Tab 4: History & Logbook
        composable(TopLevelDestination.HISTORY.route) {
            com.repforge.feature.history.HistoryScreen()
        }

        // Tab 5: Profile & Biometrics
        composable(TopLevelDestination.PROFILE.route) {
            com.repforge.feature.profile.ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
    }
}
