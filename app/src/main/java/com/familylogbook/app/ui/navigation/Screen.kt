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
    
    data class PersonProfile(val personId: String) : Screen("person_profile/{personId}") {
        companion object {
            const val ROUTE = "person_profile/{personId}"
            fun createRoute(personId: String) = "person_profile/$personId"
        }
    }
    
    data class EntityProfile(val entityId: String) : Screen("entity_profile/{entityId}") {
        companion object {
            const val ROUTE = "entity_profile/{entityId}"
            fun createRoute(entityId: String) = "entity_profile/$entityId"
        }
    }
}

