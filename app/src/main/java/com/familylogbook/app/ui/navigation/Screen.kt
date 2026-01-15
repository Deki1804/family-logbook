package com.familylogbook.app.ui.navigation

sealed class Screen(val route: String) {
    // Parent OS Main Tabs
    object Health : Screen("health") // Medicine, Symptom, Vaccination tracking
    object Day : Screen("day") // Daily routines, checklists
    object Child : Screen("child") // Child profiles
    object Insights : Screen("insights") // Analytics, reports (formerly Stats)
    
    // Legacy routes (kept for backward compatibility)
    object Home : Screen("home") // Maps to Health tab
    object Stats : Screen("stats") // Maps to Insights tab
    
    // Common screens
    object Settings : Screen("settings")
    object AddEntry : Screen("add_entry")
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Welcome : Screen("welcome") // Welcome/Auth screen - first screen for new users
    
    companion object {
        fun HomeWithCategory(category: String) = "home?category=$category"
    }
    
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

    data class EntryDetail(val entryId: String) : Screen("entry_detail/{entryId}") {
        companion object {
            const val ROUTE = "entry_detail/{entryId}"
            fun createRoute(entryId: String) = "entry_detail/$entryId"
        }
    }
}

