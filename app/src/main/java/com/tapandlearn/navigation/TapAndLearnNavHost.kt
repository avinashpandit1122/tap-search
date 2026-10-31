package com.tapandlearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tapandlearn.feature.flashcards.FlashcardsScreen
import com.tapandlearn.feature.library.LibraryScreen
import com.tapandlearn.feature.onboarding.OnboardingScreen
import com.tapandlearn.feature.onboarding.SplashScreen
import com.tapandlearn.feature.quiz.QuizScreen
import com.tapandlearn.feature.reader.PdfReaderScreen

@Composable
fun TapAndLearnNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Dashboard with Bottom Navigation Tabs
        composable(Screen.Main.route) {
            MainContainerScreen(
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                onOpenDocument = { docId, docTitle ->
                    navController.navigate(Screen.Reader.createRoute(docId, docTitle))
                },
                onNavigateToFlashcards = { navController.navigate(Screen.Flashcards.route) },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) }
            )
        }

        // Document Library Screen
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDocument = { docId, docTitle ->
                    navController.navigate(Screen.Reader.createRoute(docId, docTitle))
                }
            )
        }

        // PDF Reader Screen
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("docId") { type = NavType.LongType },
                navArgument("docTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getLong("docId") ?: 1L
            val docTitle = backStackEntry.arguments?.getString("docTitle") ?: "Document.pdf"
            PdfReaderScreen(
                docId = docId,
                docTitle = docTitle,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Flashcards Revision Screen
        composable(Screen.Flashcards.route) {
            FlashcardsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Quiz Screen
        composable(Screen.Quiz.route) {
            QuizScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
