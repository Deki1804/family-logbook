package com.familylogbook.app

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import java.net.URLDecoder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.firebase.auth.FirebaseAuth
import com.familylogbook.app.ui.screen.SettingsScreen
import com.familylogbook.app.data.repository.FirestoreSeedData
import com.familylogbook.app.data.repository.InMemoryLogbookRepository
import com.familylogbook.app.domain.classifier.EntryClassifier
import com.familylogbook.app.domain.repository.LogbookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.familylogbook.app.ui.navigation.Screen
import com.familylogbook.app.ui.screen.AddEntryScreen
import com.familylogbook.app.ui.screen.AdviceDetailScreen
import com.familylogbook.app.ui.screen.ChildProfileScreen
import com.familylogbook.app.ui.screen.PersonProfileScreen
import com.familylogbook.app.ui.screen.EntityProfileScreen
import com.familylogbook.app.ui.screen.CategoryDetailScreen
import com.familylogbook.app.ui.screen.HomeScreen
import com.familylogbook.app.ui.screen.EntryDetailScreen
import com.familylogbook.app.ui.screen.LoginScreen
import com.familylogbook.app.ui.screen.OnboardingScreen
import com.familylogbook.app.ui.screen.SettingsScreen
import com.familylogbook.app.ui.screen.StatsScreen
import com.familylogbook.app.ui.screen.SplashScreen
import com.familylogbook.app.ui.theme.FamilyOSTheme
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start reminder scheduler for notifications
        val reminderScheduler = ReminderScheduler(this)
        reminderScheduler.startPeriodicReminderCheck()
        
        setContent {
            FamilyOSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (useFirestore) {
                        // Show splash screen and initialize auth/repository async
                        SplashScreenWrapper(
                            authManager = authManager,
                            classifier = classifier
                        ) { repository ->
                            FamilyOSApp(
                                repository = repository,
                                classifier = classifier,
                                authManager = authManager
                            )
                        }
                    } else {
                        // Use in-memory repository immediately (no auth needed)
                        FamilyOSApp(
                            repository = InMemoryLogbookRepository(),
                            classifier = classifier,
                            authManager = null
                        )
                    }
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

/**
 * Wrapper composable that handles async auth initialization with splash screen.
 */
@Composable
fun SplashScreenWrapper(
    authManager: AuthManager,
    classifier: EntryClassifier,
    content: @Composable (LogbookRepository) -> Unit
) {
    var repository by remember { mutableStateOf<LogbookRepository?>(null) }
    var authError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Handle auth initialization in LaunchedEffect (proper @Composable context)
    LaunchedEffect(Unit) {
        try {
            // Small delay for splash screen visibility
            kotlinx.coroutines.delay(500)
            
            // Ensure user is signed in (async, no blocking)
            val userId = authManager.ensureSignedIn()
            val currentUser = FirebaseAuth.getInstance().currentUser
            android.util.Log.d("SplashScreenWrapper", "User signed in - userId: $userId, auth uid: ${currentUser?.uid}, isAnonymous: ${currentUser?.isAnonymous}")
            
            // Verify userId matches auth uid
            if (currentUser != null && currentUser.uid != userId) {
                android.util.Log.e("SplashScreenWrapper", "WARNING: userId mismatch! Repository: $userId, Auth: ${currentUser.uid}")
            }
            
            // Store userId in shared preferences for ReminderWorker
            context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("user_id", userId)
                .apply()
            
            // Initialize repository (no userId needed - it uses FirebaseAuth dynamically)
            val repo = try {
                FirestoreLogbookRepository()
            } catch (e: Exception) {
                android.util.Log.e("SplashScreenWrapper", "Firebase initialization failed: ${e.message}", e)
                android.util.Log.w("SplashScreenWrapper", "Falling back to in-memory repository")
                InMemoryLogbookRepository()
            }
            
            // Sample data seeding disabled - users start with empty database
            
            // Small delay to show completion
            kotlinx.coroutines.delay(300)
            
            repository = repo
        } catch (e: Exception) {
            android.util.Log.e("SplashScreenWrapper", "Auth failed, using in-memory repository", e)
            repository = InMemoryLogbookRepository()
            authError = true
        }
    }
    
    // Show splash screen while loading
    if (repository == null && !authError) {
        SplashScreen(
            authManager = authManager,
            onAuthReady = { /* Not used - handled in LaunchedEffect above */ },
            onError = { /* Not used - handled in LaunchedEffect above */ }
        )
    }
    
    // Render content when repository is ready
    repository?.let { repo ->
        content(repo)
    }
}

@Composable
fun FamilyOSApp(
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
    
    // Reactive state for onboarding completion
    var onboardingCompleted by remember { 
        mutableStateOf(sharedPrefs.getBoolean("onboarding_completed", false)) 
    }
    
    // Auto-complete onboarding if user already has persons (for existing users)
    LaunchedEffect(persons) {
        if (persons.isNotEmpty() && !sharedPrefs.getBoolean("onboarding_completed", false)) {
            sharedPrefs.edit()
                .putBoolean("onboarding_completed", true)
                .apply()
            onboardingCompleted = true
        }
    }
    
    // Update state when route changes (onboarding completion sets shared prefs)
    LaunchedEffect(currentRoute) {
        val completed = sharedPrefs.getBoolean("onboarding_completed", false)
        if (completed != onboardingCompleted) {
            onboardingCompleted = completed
        }
    }
    
    // Determine if onboarding is needed
    val needsOnboarding = remember(onboardingCompleted, persons) { 
        !onboardingCompleted && persons.isEmpty()
    }
    
    // Navigate to onboarding if needed (but don't force if already on onboarding)
    LaunchedEffect(needsOnboarding, currentRoute) {
        if (needsOnboarding && currentRoute != Screen.Onboarding.route && currentRoute != null) {
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
            android.util.Log.w("FamilyOSApp", "Notification permission denied")
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
                // Use activity scope to share ViewModel instance with advice_detail screen
                val activity = LocalContext.current as? ComponentActivity
                val context = LocalContext.current
                val viewModel: HomeViewModel = if (activity != null) {
                    viewModel(
                        viewModelStoreOwner = activity
                    ) {
                        HomeViewModel(repository, context)
                    }
                } else {
                    viewModel { HomeViewModel(repository, context) }
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
                    onNavigateToAddEntryWithText = { text ->
                        // Navigate to AddEntry with pre-filled text
                        navController.navigate("${Screen.AddEntry.route}?text=${Uri.encode(text)}")
                    },
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate("${Screen.AddEntry.route}?entryId=$entryId")
                    },
                    onNavigateToEntryDetail = { entryId ->
                        navController.navigate(Screen.EntryDetail.createRoute(entryId))
                    },
                    onNavigateToPersonProfile = { personId ->
                        navController.navigate(Screen.PersonProfile.createRoute(personId))
                    },
                    onNavigateToEntityProfile = { entityId ->
                        navController.navigate(Screen.EntityProfile.createRoute(entityId))
                    },
                    onNavigateToCategoryDetail = { category ->
                        // Navigate to category detail screen
                        navController.navigate("category_detail/${category.name}")
                    },
                    onNavigateToAdvice = {
                        // Scroll to advice pills section (or could navigate to advice screen)
                        // For now, just scroll to first advice pill
                        // This will be handled by scrolling in HomeScreen
                    },
                    onNavigateToAdviceDetail = { advice ->
                        // Store advice in current ViewModel FIRST, then navigate
                        // This ensures advice is available when advice_detail screen loads
                        android.util.Log.d("MainActivity", "Navigating to advice detail: ${advice.id}, title: ${advice.title}")
                        viewModel.setCurrentAdvice(advice)
                        // Verify advice was set before navigating
                        val adviceSet = viewModel.currentAdvice.value
                        android.util.Log.d("MainActivity", "Advice set in ViewModel: ${adviceSet?.id}, same instance: ${adviceSet === advice}")
                        if (adviceSet != null) {
                            // Navigate with advice ID as argument for safety
                            navController.navigate("advice_detail?adviceId=${advice.id}")
                        } else {
                            android.util.Log.e("MainActivity", "Failed to set advice in ViewModel!")
                        }
                    }
                )
            }
            
            composable(
                route = "${Screen.AddEntry.route}?entryId={entryId}&entityId={entityId}&category={category}&text={text}",
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
                    },
                    navArgument("text") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId")
                val entityId = backStackEntry.arguments?.getString("entityId")
                val categoryParam = backStackEntry.arguments?.getString("category")
                val textParam = backStackEntry.arguments?.getString("text")?.let { 
                    try {
                        URLDecoder.decode(it, "UTF-8")
                    } catch (e: Exception) {
                        it
                    }
                }
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
                    entryId = entryId,
                    initialText = textParam
                )
            }

            composable(
                route = Screen.EntryDetail.ROUTE,
                arguments = listOf(
                    navArgument("entryId") {
                        type = androidx.navigation.NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                val context = LocalContext.current
                val activity = context as? ComponentActivity
                val homeViewModel: HomeViewModel = if (activity != null) {
                    viewModel(viewModelStoreOwner = activity) { HomeViewModel(repository, context) }
                } else {
                    viewModel { HomeViewModel(repository, context) }
                }

                EntryDetailScreen(
                    entryId = entryId,
                    viewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditClick = { id ->
                        navController.navigate("${Screen.AddEntry.route}?entryId=$id")
                    }
                )
            }
            
            composable(Screen.Stats.route) {
                val context = LocalContext.current
                val statsViewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository, context)
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
                val context = LocalContext.current
                val statsViewModel: StatsViewModel = viewModel {
                    StatsViewModel(repository)
                }
                val homeViewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository, context)
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
            
            composable(
                route = "advice_detail?adviceId={adviceId}",
                arguments = listOf(
                    navArgument("adviceId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                // Use shared ViewModel scope - get from activity to use same instance as HomeScreen
                val context = LocalContext.current
                val activity = context as? ComponentActivity
                val homeViewModel: HomeViewModel = if (activity != null) {
                    // Use activity scope to get the same ViewModel instance as HomeScreen
                    viewModel(
                        viewModelStoreOwner = activity
                    ) {
                        HomeViewModel(repository, context)
                    }
                } else {
                    // Fallback to regular viewModel (shouldn't happen)
                    viewModel { HomeViewModel(repository, context) }
                }
                
                val adviceIdParam = backStackEntry.arguments?.getString("adviceId")
                val adviceFromState by homeViewModel.currentAdvice.collectAsState()
                val scope = rememberCoroutineScope()
                
                // Try to get advice from ViewModel state first, then from AdviceEngine by ID
                val adviceEngine = remember { com.familylogbook.app.domain.classifier.AdviceEngine() }
                val advice = remember(adviceFromState, adviceIdParam) {
                    adviceFromState ?: adviceIdParam?.let { id ->
                        adviceEngine.getAdviceById(id)
                    }
                }
                
                // Debug log
                LaunchedEffect(advice, adviceIdParam) {
                    android.util.Log.d("AdviceDetail", "Advice state: ${adviceFromState?.id}, param: $adviceIdParam, final: ${advice?.id}")
                }
                
                // Clean up advice when leaving this screen (after navigation completes)
                DisposableEffect(Unit) {
                    onDispose {
                        // Clear advice after a short delay to ensure navigation completed
                        scope.launch {
                            kotlinx.coroutines.delay(200)
                            homeViewModel.setCurrentAdvice(null)
                        }
                    }
                }
                
                // If advice is null on initial load, wait a bit for state to propagate
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(150) // Small delay to allow state to propagate from HomeScreen
                    if (advice == null) {
                        // Advice still null after delay - navigate back
                        android.util.Log.w("AdviceDetail", "Advice is null after delay, navigating back")
                        navController.popBackStack()
                    }
                }
                
                // Show screen only if advice exists
                advice?.let { currentAdvice ->
                    AdviceDetailScreen(
                        advice = currentAdvice,
                        onNavigateBack = {
                            // Navigate back first, advice will be cleared in DisposableEffect
                            navController.popBackStack()
                        }
                    )
                } ?: run {
                    // Show loading state while advice is null (brief moment)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
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
                val context = LocalContext.current
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository, context)
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
                val context = LocalContext.current
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository, context)
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
                val context = LocalContext.current
                val viewModel: HomeViewModel = viewModel {
                    HomeViewModel(repository, context)
                }
                EntityProfileScreen(
                    entityId = entityId,
                    viewModel = viewModel
                )
            }
        }
    }
}

