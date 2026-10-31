package com.tapandlearn.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Library : Screen("library")
    object Reader : Screen("reader/{docId}/{docTitle}") {
        fun createRoute(docId: Long, docTitle: String) = "reader/$docId/$docTitle"
    }
    object Flashcards : Screen("flashcards")
    object Quiz : Screen("quiz")
}

sealed class BottomTab(val route: String, val title: String) {
    object Home : BottomTab("tab_home", "Home")
    object Dictionary : BottomTab("tab_dictionary", "Dictionary")
    object Saved : BottomTab("tab_saved", "Saved")
    object Stats : BottomTab("tab_stats", "Stats")
    object Settings : BottomTab("tab_settings", "Settings")
}
