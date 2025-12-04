package com.familylogbook.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.familylogbook.app.data.auth.AuthManager
import com.familylogbook.app.data.notification.ReminderScheduler
import com.familylogbook.app.data.repository.FirestoreLogbookRepository
import com.familylogbook.app.ui.screen.SettingsScreen
import com.familylogbook.app.data.repository.FirestoreSeedData
import com.familylogbook.app.data.repository.InMemoryLogbookRepository
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.screen.AddEntryScreen
import com.familylogbook.app.ui.screen.ChildProfileScreen
import com.familylogbook.app.ui.screen.PersonProfileScreen
import com.familylogbook.app.ui.screen.EntityProfileScreen
import com.familylogbook.app.ui.screen.CategoryDetailScreen
import com.familylogbook.app.ui.screen.HomeScreen
import com.familylogbook.app.ui.screen.LoginScreen
import com.familylogbook.app.ui.screen.OnboardingScreen
import com.familylogbook.app.ui.screen.SettingsScreen
import com.familylogbook.app.ui.screen.StatsScreen
import com.familylogbook.app.ui.theme.FamilyLogbookTheme
import com.familylogbook.app.ui.viewmodel.AddEntryViewModel
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import com.familylogbook.app.ui.viewmodel.StatsViewModel

class MainActivity : ComponentActivity() {
    
    // Simple DI - in a real app, use Hilt or Koin
    // Switch between InMemoryLogbookRepository (for testing) and FirestoreLogbookRepository (for production)
    private val useFirestore = true // Set to false to use in-memory repository
    private val authManager = AuthManager()
    private val classifier = EntryClassifier()
    
    // Repository will be initialized after Auth
    private lateinit var repository: LogbookRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository based on Auth
        repository = if (useFirestore) {
            try {
                // Ensure user is signed in (anonymous if needed) before creating repository
                val userId = runBlocking {
                    authManager.ensureSignedIn()
                }
                
                // Store userId in shared preferences for ReminderWorker
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_id", userId)
                    .apply()
                
                // Seed Firestore with sample data if database is empty
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        FirestoreSeedData.seedIfEmpty(userId)
                    } catch (e: Exception) {
                        // Ignore errors - database might already have data
                        android.util.Log.e("MainActivity", "Error seeding Firestore: ${e.message}")
                    }
                }
                
                FirestoreLogbookRepository(userId = userId)
            } catch (e: Exception) {
                // Fallback to in-memory repository if Firebase fails
                android.util.Log.e("MainActivity", "Firebase initialization failed: ${e.message}", e)
                android.util.Log.w("MainActivity", "Falling back to in-memory repository")
                InMemoryLogbookRepository()
            }
        } else {
            InMemoryLogbookRepository()
        }
        
        // Start reminder scheduler for notifications
        val reminderScheduler = ReminderScheduler(this)
        reminderScheduler.startPeriodicReminderCheck()
        
        setContent {
            FamilyLogbookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamilyLogbookApp(
                        repository = repository,
                        classifier = classifier,
                        authManager = if (useFirestore) authManager else null
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
    object Home : BottomNavItem(Screen.Home.route, "Početna", Icons.Default.Home)
    object Stats : BottomNavItem(Screen.Stats.route, "Statistika", Icons.Default.ShowChart)
    object Settings : BottomNavItem(Screen.Settings.route, "Postavke", Icons.Default.Settings)
}

@Composable
fun FamilyLogbookApp(
    repository: LogbookRepository,
    classifier: EntryClassifier,
    authManager: AuthManager? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val context = LocalContext.current
    
    // Check onboarding status
    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(repository)
    }
    val persons by settingsViewModel.persons.collectAsState()
    
    val sharedPrefs = remember { 
        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) 
    }
    
    // Auto-complete onboarding if user already has persons (for existing users)
    LaunchedEffect(persons) {
        if (persons.isNotEmpty() && !sharedPrefs.getBoolean("onboarding_completed", false)) {
            sharedPrefs.edit()
                .putBoolean("onboarding_completed", true)
                .apply()
        }
    }
    
    // Determine start destination - if user has persons, onboarding is automatically complete
    val onboardingCompleted = remember(persons) { 
        persons.isNotEmpty() || sharedPrefs.getBoolean("onboarding_completed", false)
    }
    val needsOnboarding = !onboardingCompleted && persons.isEmpty()
    
    // Navigate to onboarding if needed
    LaunchedEffect(needsOnboarding, currentRoute) {
        if (needsOnboarding && currentRoute != Screen.Onboarding.route) {
            navController.navigate(Screen.Onboarding.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Check and request notification permission for Android 13+
    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Permission denied - notifications won't work
            // User can enable it later in Settings
            android.util.Log.w("FamilyLogbookApp", "Notification permission denied")
        }
    }
    
    // Request notification permission on first launch (Android 13+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationsEnabled) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Stats,
        BottomNavItem.Settings
    )
    
    Scaffold(
        bottomBar = {
            // Check if current route starts with any of the main routes (to handle query params)
            val isMainRoute = currentRoute?.startsWith(Screen.Home.route) == true ||
                    currentRoute == Screen.Stats.route ||
                    currentRoute == Screen.Settings.route
            
            if (isMainRoute) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        // Check if current route matches this item (handle query params for Home)
                        val isSelected = when {
                            item.route == Screen.Home.route -> currentRoute?.startsWith(Screen.Home.route) == true
                            else -> currentRoute == item.route
                        }
                        
                        NavigationBarItem(
                            selected = isSelected,
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
            startDestination = if (needsOnboarding) Screen.Onboarding.route else Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Onboarding screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    settingsViewModel = settingsViewModel,
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            // Home route with optional category and person filters
            composable(
                route = "${Screen.Home.route}?category={category}&person={person}",
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    },
                    navArgument("person") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                
                // Set category and person filters if provided
                val categoryParam = backStackEntry.arguments?.getString("category")
                val personParam = backStackEntry.arguments?.getString("person")
                
                androidx.compose.runtime.LaunchedEffect(categoryParam, personParam) {
                    if (!categoryParam.isNullOrEmpty()) {
                        try {
                            val category = com.familylogbook.app.domain.model.Category.valueOf(categoryParam)
                            viewModel.setSelectedCategory(category)
                        } catch (e: Exception) {
                            // Invalid category, ignore
                        }
                    }
                    if (!personParam.isNullOrEmpty()) {
                        viewModel.setSelectedPerson(personParam)
                    }
                }
                
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAddEntry = {
                        navController.navigate(Screen.AddEntry.route)
                    },
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate("${Screen.AddEntry.route}?entryId=$entryId")
                    },
                    onNavigateToPersonProfile = { personId ->
                        navController.navigate(Screen.PersonProfile.createRoute(personId))
                    },
                    onNavigateToEntityProfile = { entityId ->
                        navController.navigate(Screen.EntityProfile.createRoute(entityId))
                    },
                    onNavigateToCategoryDetail = { category ->
                        // Category filtering is handled internally
                    }
                )
            }
            
            composable(
                route = "${Screen.AddEntry.route}?entryId={entryId}&entityId={entityId}&category={category}",
                arguments = listOf(
                    navArgument("entryId") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("entityId") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("category") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")
                val entityId = backStackEntry.arguments?.getString("entityId")
                val categoryParam = backStackEntry.arguments?.getString("category")
                val viewModel: AddEntryViewModel = viewModel {
                    AddEntryViewModel(repository, classifier)
                }
                
                // Pre-populate entity/category if provided (quick actions from entity)
                androidx.compose.runtime.LaunchedEffect(entityId, categoryParam) {
                    if (entityId != null) {
                        viewModel.setSelectedEntity(entityId)
                    }
                    // Category is currently inferred from text; param reserved for future use
                }
                AddEntryScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    entryId = entryId
                )
            }
            
            composable(Screen.Stats.route) {
                val statsViewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                
                // Get currently selected person from HomeViewModel
                val selectedPersonId by homeViewModel.selectedPersonId.collectAsState()
                
                StatsScreen(
                    viewModel = statsViewModel,
                    onCategoryClick = { category ->
                        // Build navigation URL with category and optional person filter
                        val personParam = if (selectedPersonId != null) "&person=$selectedPersonId" else ""
                        navController.navigate("home?category=${category.name}$personParam") {
                            // Clear back stack so back button doesn't go to Stats
                            popUpTo(Screen.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            // Don't restore scroll state when navigating with filter - always start from top
                            restoreState = false
                        }
                    }
                )
            }
            
            composable("category_detail/{categoryName}") { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val category = try {
                    com.familylogbook.app.domain.model.Category.valueOf(categoryName)
                } catch (e: Exception) {
                    com.familylogbook.app.domain.model.Category.OTHER
                }
                val statsViewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                CategoryDetailScreen(
                    category = category,
                    statsViewModel = statsViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel {
                    SettingsViewModel(repository)
                }
                SettingsScreen(
                    viewModel = viewModel,
                    authManager = authManager,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToAddEntryForEntity = { entityId, category ->
                        navController.navigate("${Screen.AddEntry.route}?entityId=$entityId&category=${category.name}")
                    }
                )
            }
            
            composable(Screen.Login.route) {
                authManager?.let { auth ->
                    LoginScreen(
                        authManager = auth,
                        isAnonymous = auth.isAnonymous(),
                        onUpgradeSuccess = {
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                } ?: run {
                    // No auth manager - shouldn't happen, but handle gracefully
                    Text("Autentifikacija nije dostupna")
                }
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
            
            composable(
                route = Screen.PersonProfile.ROUTE,
                arguments = listOf(navArgument("personId") { type = NavType.StringType })
            ) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId") ?: return@composable
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                PersonProfileScreen(
                    personId = personId,
                    viewModel = viewModel
                )
            }
            
            composable(
                route = Screen.EntityProfile.ROUTE,
                arguments = listOf(navArgument("entityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val entityId = backStackEntry.arguments?.getString("entityId") ?: return@composable
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                EntityProfileScreen(
                    entityId = entityId,
                    viewModel = viewModel
                )
            }
            
            composable("category_detail/{categoryName}") { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val category = try {
                    com.familylogbook.app.domain.model.Category.valueOf(categoryName)
                } catch (e: Exception) {
                    com.familylogbook.app.domain.model.Category.OTHER
                }
                val statsViewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository)
                }
                CategoryDetailScreen(
                    category = category,
                    statsViewModel = statsViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

