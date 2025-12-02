package com.familylogbook.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
    object AddEntry : Screen("add_entry")
    object Login : Screen("login")
    
    data class ChildProfile(val childId: String) : Screen("child_profile/{childId}") {
        companion object {
            const val ROUTE = "child_profile/{childId}"
            fun createRoute(childId: String) = "child_profile/$childId"
        }
    }
}

