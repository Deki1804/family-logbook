package com.familylogbook.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object AddEntry : Screen("add_entry")
}

