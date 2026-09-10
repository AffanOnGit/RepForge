package com.repforge.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.repforge.feature.auth.LoginScreen
import com.repforge.feature.auth.SignUpScreen
import com.repforge.feature.onboarding.OnboardingScreen
import com.repforge.feature.routines.CustomRoutineBuilderScreen
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

        // Tab 1: Today / Active Session (freestyle)
        composable(TopLevelDestination.TODAY.route) {
            com.repforge.feature.session.ActiveSessionScreen(
                onFinishWorkout = { sessionId ->
                    navController.navigate("workout_summary/$sessionId")
                },
                onNavigateToProfile = {
                    navController.navigate(TopLevelDestination.PROFILE.route)
                }
            )
        }

        // Active session started from a routine
        composable(
            route = "active_session/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            com.repforge.feature.session.ActiveSessionScreen(
                onFinishWorkout = { sessionId ->
                    navController.navigate("workout_summary/$sessionId")
                },
                onNavigateToProfile = {
                    navController.navigate(TopLevelDestination.PROFILE.route)
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
                onCreateCustomRoutineClick = { navController.navigate("custom_routine_builder") },
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

        // Custom Routine Builder — create mode (no routineId)
        composable("custom_routine_builder") {
            CustomRoutineBuilderScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoutineSaved = { routineId ->
                    navController.navigate("routine_detail/$routineId") {
                        popUpTo("custom_routine_builder") { inclusive = true }
                    }
                }
            )
        }

        // Custom Routine Builder — edit mode (routineId provided)
        composable(
            route = "custom_routine_builder/{routineId}",
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) {
            CustomRoutineBuilderScreen(
                onNavigateBack = { navController.popBackStack() },
                onRoutineSaved = { routineId ->
                    navController.navigate("routine_detail/$routineId") {
                        popUpTo("custom_routine_builder/{routineId}") { inclusive = true }
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
                },
                onEditRoutine = { routineId ->
                    navController.navigate("custom_routine_builder/$routineId")
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
