package com.familylogbook.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.ui.viewmodel.AddEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun AddEntryScreen(
    viewModel: AddEntryViewModel,
    onNavigateBack: () -> Unit
) {
    val children by viewModel.children.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val entryText by viewModel.entryText.collectAsState()
    
    var showQuickInputs by remember { mutableStateOf(false) }
    var selectedQuickCategory by remember { mutableStateOf<Category?>(null) }
    var temperatureValue by remember { mutableStateOf("") }
    var amountValue by remember { mutableStateOf("") }
    var currencyValue by remember { mutableStateOf("EUR") }
    val isFeedingActive by viewModel.isFeedingActive.collectAsState()
    val feedingElapsedSeconds by viewModel.feedingElapsedSeconds.collectAsState()
    val selectedFeedingType by viewModel.selectedFeedingType.collectAsState()
    val bottleAmount by viewModel.bottleAmount.collectAsState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Entry") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (viewModel.saveEntry()) {
                                    onNavigateBack()
                                }
                                // If save fails, entry text validation will prevent saving
                                // In production, show snackbar with error message
                            }
                        },
                        enabled = entryText.trim().isNotEmpty()
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Quick Feeding Tracker
            if (!isFeedingActive) {
                QuickFeedingButtons(
                    children = children,
                    selectedChildId = selectedChildId,
                    onSelectChild = { viewModel.setSelectedChild(it) },
                    onStartFeeding = { type ->
                        if (selectedChildId != null) {
                            viewModel.startFeeding(type)
                        }
                    },
                    enabled = selectedChildId != null
                )
            } else {
                FeedingTimerCard(
                    elapsedSeconds = feedingElapsedSeconds,
                    feedingType = selectedFeedingType,
                    bottleAmount = bottleAmount,
                    onBottleAmountChange = { viewModel.setBottleAmount(it) },
                    onStop = { viewModel.stopFeeding() },
                    onSave = {
                        scope.launch {
                            if (viewModel.saveFeedingEntry()) {
                                onNavigateBack()
                            }
                        }
                    }
                )
            }
            
            Divider()
            
            // Person/Entity selection
            Text(
                text = "Who/What is this about?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            // Person selection
            if (persons.isNotEmpty()) {
                Text(
                    text = "People",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Family option
                    PersonChip(
                        label = "Family",
                        isSelected = selectedPersonId == null && selectedEntityId == null && selectedChildId == null,
                        onClick = {
                            viewModel.setSelectedPerson(null)
                            viewModel.setSelectedEntity(null)
                            viewModel.setSelectedChild(null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Persons chips
                    persons.forEach { person ->
                        PersonChip(
                            label = person.name,
                            emoji = person.emoji,
                            color = Color(android.graphics.Color.parseColor(person.avatarColor)),
                            isSelected = selectedPersonId == person.id,
                            onClick = {
                                viewModel.setSelectedPerson(person.id)
                                viewModel.setSelectedEntity(null)
                                viewModel.setSelectedChild(null)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Legacy Children (backward compatibility)
            if (children.isNotEmpty() && persons.isEmpty()) {
                Text(
                    text = "Children",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChildChip(
                        label = "Family",
                        isSelected = selectedChildId == null,
                        onClick = { viewModel.setSelectedChild(null) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    children.forEach { child ->
                        ChildChip(
                            label = child.name,
                            emoji = child.emoji,
                            color = Color(android.graphics.Color.parseColor(child.avatarColor)),
                            isSelected = selectedChildId == child.id,
                            onClick = { viewModel.setSelectedChild(child.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Entity selection
            if (entities.isNotEmpty()) {
                Text(
                    text = "Entities",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    entities.forEach { entity ->
                        EntityChip(
                            label = entity.name,
                            emoji = entity.emoji,
                            color = Color(android.graphics.Color.parseColor(entity.avatarColor)),
                            isSelected = selectedEntityId == entity.id,
                            onClick = {
                                viewModel.setSelectedEntity(entity.id)
                                viewModel.setSelectedPerson(null)
                                viewModel.setSelectedChild(null)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Divider()
            
            // Quick inputs toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Inputs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = showQuickInputs,
                    onCheckedChange = { showQuickInputs = it }
                )
            }
            
            // Quick inputs for different categories
            if (showQuickInputs) {
                QuickInputsSection(
                    selectedCategory = selectedQuickCategory,
                    onCategorySelected = { selectedQuickCategory = it },
                    temperatureValue = temperatureValue,
                    onTemperatureChange = { temperatureValue = it },
                    amountValue = amountValue,
                    onAmountChange = { amountValue = it },
                    currencyValue = currencyValue,
                    onCurrencyChange = { currencyValue = it },
                    onAddToText = { text ->
                        viewModel.setEntryText(entryText + (if (entryText.isNotEmpty()) " " else "") + text)
                    }
                )
            }
            
            // Text input
            Text(
                text = "What happened?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = entryText,
                onValueChange = { viewModel.setEntryText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("E.g., Neo had a light fever tonight. Gave him some medicine.") },
                minLines = 5,
                maxLines = 10
            )
            
            Text(
                text = "The app will automatically categorize your entry and add relevant tags.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PersonChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            color.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            BorderStroke(2.dp, color)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emoji?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EntityChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            color.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            BorderStroke(2.dp, color)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emoji?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun ChildChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            color.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            BorderStroke(2.dp, color)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            emoji?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun QuickInputsSection(
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    temperatureValue: String,
    onTemperatureChange: (String) -> Unit,
    amountValue: String,
    onAmountChange: (String) -> Unit,
    currencyValue: String,
    onCurrencyChange: (String) -> Unit,
    onAddToText: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Inputs",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Category selection
            Text(
                text = "Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Category.HEALTH,
                    Category.AUTO,
                    Category.HOUSE,
                    Category.FINANCE,
                    Category.WORK,
                    Category.SHOPPING
                ).forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.name, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Temperature input (for HEALTH)
            if (selectedCategory == Category.HEALTH) {
                OutlinedTextField(
                    value = temperatureValue,
                    onValueChange = onTemperatureChange,
                    label = { Text("Temperature (°C)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (temperatureValue.isNotEmpty()) {
                                    onAddToText("Temperatura $temperatureValue°C")
                                    onTemperatureChange("")
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                )
            }
            
            // Amount input (for FINANCE)
            if (selectedCategory == Category.FINANCE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountValue,
                        onValueChange = onAmountChange,
                        label = { Text("Amount") },
                        modifier = Modifier.weight(2f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    OutlinedTextField(
                        value = currencyValue,
                        onValueChange = onCurrencyChange,
                        label = { Text("Currency") },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            if (amountValue.isNotEmpty()) {
                                onAddToText("$amountValue $currencyValue")
                                onAmountChange("")
                            }
                        },
                        modifier = Modifier.align(Alignment.Bottom)
                    ) {
                        Text("Add")
                    }
                }
            }
            
            // Quick phrases based on category
            Text(
                text = "Quick Phrases",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (selectedCategory) {
                    Category.AUTO -> {
                        listOf("Probušena guma", "Servis auta", "Registracija", "Osiguranje").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                    Category.HOUSE -> {
                        listOf("Pokvario se", "Popravak", "Filter", "Čišćenje").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                    Category.FINANCE -> {
                        listOf("Račun", "Plaćanje", "Struja", "Voda").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                    Category.WORK -> {
                        listOf("Sastanak", "Rok", "Projekt", "Klijent").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                    Category.SHOPPING -> {
                        listOf("Kupovina", "Namirnice", "Lista", "Treba kupiti").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                    else -> {
                        // Default phrases
                        listOf("Danas", "Sada", "Važno").forEach { phrase ->
                            SuggestionChip(
                                onClick = { onAddToText(phrase) },
                                label = { Text(phrase, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

