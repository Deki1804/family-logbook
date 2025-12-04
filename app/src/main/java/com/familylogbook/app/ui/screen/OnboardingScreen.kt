package com.familylogbook.app.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.PersonType
import com.familylogbook.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    settingsViewModel: SettingsViewModel,
    onComplete: () -> Unit,
    onSkip: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val persons by settingsViewModel.persons.collectAsState()
    val entities by settingsViewModel.entities.collectAsState()
    val newPersonName by settingsViewModel.newPersonName.collectAsState()
    val newPersonType by settingsViewModel.newPersonType.collectAsState()
    val newPersonEmoji by settingsViewModel.newPersonEmoji.collectAsState()
    val newEntityName by settingsViewModel.newEntityName.collectAsState()
    val newEntityType by settingsViewModel.newEntityType.collectAsState()
    val newEntityEmoji by settingsViewModel.newEntityEmoji.collectAsState()
    
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val scope = rememberCoroutineScope()
    
    var showAddPersonForm by remember { mutableStateOf(false) }
    var showAddEntityForm by remember { mutableStateOf(false) }
    
    // Check if user has at least one person - if yes, complete onboarding
    LaunchedEffect(persons) {
        if (persons.isNotEmpty() && pagerState.currentPage < 3) {
            // User added a person, move to completion page
            scope.launch {
                pagerState.animateScrollToPage(3)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                // Mark onboarding as completed when skipped
                val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putBoolean("onboarding_completed", true)
                    .apply()
                onSkip()
            }) {
                Text("Preskoči", fontSize = 14.sp)
            }
        }
        
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingWelcomePage()
                1 -> OnboardingAddPersonPage(
                    showForm = showAddPersonForm,
                    onShowFormChange = { showAddPersonForm = it },
                    settingsViewModel = settingsViewModel,
                    newPersonName = newPersonName,
                    newPersonType = newPersonType,
                    newPersonEmoji = newPersonEmoji,
                    persons = persons
                )
                2 -> OnboardingAddEntityPage(
                    showForm = showAddEntityForm,
                    onShowFormChange = { showAddEntityForm = it },
                    settingsViewModel = settingsViewModel,
                    newEntityName = newEntityName,
                    newEntityType = newEntityType,
                    newEntityEmoji = newEntityEmoji,
                    entities = entities,
                    onSkip = {
                        scope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
                3 -> OnboardingCompletePage()
            }
        }
        
        // Page indicators and navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }
            
            // Next/Complete button
            Button(
                        onClick = {
                            scope.launch {
                                if (pagerState.currentPage < 3) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    // Mark onboarding as completed
                                    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                    sharedPrefs.edit()
                                        .putBoolean("onboarding_completed", true)
                                        .apply()
                                    onComplete()
                                }
                            }
                        },
                enabled = when (pagerState.currentPage) {
                    1 -> persons.isNotEmpty() || showAddPersonForm
                    2 -> true // Entity is optional
                    else -> true
                }
            ) {
                Text(if (pagerState.currentPage < 3) "Dalje" else "Završi")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun OnboardingWelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👨‍👩‍👧‍👦",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Dobrodošli u FamilyOS!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Vaš kompletan upravitelj obiteljskog života. Praćenje zdravlja, hranjenja, spavanja, financija i još puno toga!",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun OnboardingAddPersonPage(
    showForm: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    newPersonName: String,
    newPersonType: PersonType,
    newPersonEmoji: String,
    persons: List<com.familylogbook.app.domain.model.Person>
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👤",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Dodaj prvu osobu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Dodaj dijete, sebe ili bilo kojeg člana obitelji da možeš započeti praćenje.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (persons.isEmpty()) {
            if (!showForm) {
                Button(
                    onClick = { onShowFormChange(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj osobu")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newPersonName,
                            onValueChange = { settingsViewModel.setNewPersonName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ime") },
                            placeholder = { Text("Unesi ime") }
                        )
                        
                        Text("Tip", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(PersonType.CHILD, PersonType.PARENT, PersonType.OTHER_FAMILY_MEMBER).forEach { type ->
                                FilterChip(
                                    selected = newPersonType == type,
                                    onClick = { settingsViewModel.setNewPersonType(type) },
                                    label = { 
                                        Text(
                                            when (type) {
                                                PersonType.CHILD -> "Dijete"
                                                PersonType.PARENT -> "Roditelj"
                                                PersonType.OTHER_FAMILY_MEMBER -> "Ostalo"
                                                else -> type.name
                                            },
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Text("Emoji", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("👶", "👧", "👦", "👨", "👩", "🧒").forEach { emoji ->
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .then(
                                            if (newPersonEmoji == emoji) {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    onClick = { settingsViewModel.setNewPersonEmoji(emoji) }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    if (settingsViewModel.addPerson()) {
                                        onShowFormChange(false)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newPersonName.trim().isNotEmpty()
                        ) {
                            Text("Dodaj osobu")
                        }
                        
                        TextButton(
                            onClick = { onShowFormChange(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Odustani")
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✅ Osoba dodana!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    persons.forEach { person ->
                        Text(
                            text = "${person.emoji} ${person.name}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingAddEntityPage(
    showForm: Boolean,
    onShowFormChange: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    newEntityName: String,
    newEntityType: com.familylogbook.app.domain.model.EntityType,
    newEntityEmoji: String,
    entities: List<com.familylogbook.app.domain.model.Entity>,
    onSkip: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏠",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Dodaj entitet (opcionalno)",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Dodaj auto, kuću ili bilo koji entitet koji želiš pratiti. Ovo možete napraviti i kasnije.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (entities.isEmpty()) {
            if (!showForm) {
                Button(
                    onClick = { onShowFormChange(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj entitet")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Preskoči za sada")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newEntityName,
                            onValueChange = { settingsViewModel.setNewEntityName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Ime") },
                            placeholder = { Text("npr. Auto, Kuća, Financije") }
                        )
                        
                        Text("Tip", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                com.familylogbook.app.domain.model.EntityType.CAR,
                                com.familylogbook.app.domain.model.EntityType.HOUSE,
                                com.familylogbook.app.domain.model.EntityType.FINANCE
                            ).forEach { type ->
                                FilterChip(
                                    selected = newEntityType == type,
                                    onClick = { settingsViewModel.setNewEntityType(type) },
                                    label = { 
                                        Text(
                                            when (type) {
                                                com.familylogbook.app.domain.model.EntityType.CAR -> "Auto"
                                                com.familylogbook.app.domain.model.EntityType.HOUSE -> "Kuća"
                                                com.familylogbook.app.domain.model.EntityType.FINANCE -> "Financije"
                                                else -> type.name
                                            },
                                            fontSize = 12.sp
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Text("Emoji", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("🚗", "🏠", "💰", "🏥", "🔧").forEach { emoji ->
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .then(
                                            if (newEntityEmoji == emoji) {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    CircleShape
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    onClick = { settingsViewModel.setNewEntityEmoji(emoji) }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    if (settingsViewModel.addEntity()) {
                                        onShowFormChange(false)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = newEntityName.trim().isNotEmpty()
                        ) {
                            Text("Dodaj entitet")
                        }
                        
                        TextButton(
                            onClick = { onShowFormChange(false) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Odustani")
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✅ Entitet dodan!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    entities.forEach { entity ->
                        Text(
                            text = "${entity.emoji} ${entity.name}",
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingCompletePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Gotovo!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sve je spremno! Sada možeš početi koristiti FamilyOS i pratiti sve važne događaje u svom obiteljskom životu.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

