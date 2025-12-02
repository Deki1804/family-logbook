package com.familylogbook.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.familylogbook.app.data.repository.InMemoryLogbookRepository
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.repository.LogbookRepository
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.screen.AddEntryScreen
import com.familylogbook.app.ui.screen.ChildProfileScreen
import com.familylogbook.app.ui.screen.HomeScreen
import com.familylogbook.app.ui.screen.SettingsScreen
import com.familylogbook.app.ui.screen.StatsScreen
import com.familylogbook.app.ui.theme.FamilyLogbookTheme
import com.familylogbook.app.ui.viewmodel.AddEntryViewModel
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import com.familylogbook.app.ui.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {
    
    // Simple DI - in a real app, use Hilt or Koin
    private val repository: LogbookRepository = InMemoryLogbookRepository()
    private val classifier: EntryClassifier = EntryClassifier()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FamilyLogbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamilyLogbookApp(
                        repository = repository,
                        classifier = classifier
                    )
                }
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(Screen.Home.route, "Home", Icons.Default.Home)
    object Stats : BottomNavItem(Screen.Stats.route, "Stats", Icons.Default.ShowChart)
    object Settings : BottomNavItem(Screen.Settings.route, "Settings", Icons.Default.Settings)
}

@Composable
fun FamilyLogbookApp(
    repository: LogbookRepository,
    classifier: EntryClassifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )
    
    Scaffold(
        bottomBar = {
            if (currentRoute in listOf(Screen.Home.route, Screen.Stats.route, Screen.Settings.route)) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAddEntry = {
                        navController.navigate(Screen.AddEntry.route)
                    },
                    onNavigateToChildProfile = { childId ->
                        navController.navigate(Screen.ChildProfile.createRoute(childId))
                    }
                )
            }
            
            composable(Screen.AddEntry.route) {
                val viewModel: AddEntryViewModel = viewModel {
                    AddEntryViewModel(repository, classifier)
                }
                AddEntryScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Stats.route) {
                val viewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                StatsScreen(viewModel = viewModel)
            }
            
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(repository)
                }
                SettingsScreen(viewModel = viewModel)
            }
            
            composable(
                route = Screen.ChildProfile.ROUTE,
                arguments = listOf(navArgument("childId") { type = NavType.StringType })
            ) { backStackEntry ->
                val childId = backStackEntry.arguments?.getString("childId") ?: return@composable
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                ChildProfileScreen(
                    childId = childId,
                    viewModel = viewModel
                )
            }
        }
    }
}

