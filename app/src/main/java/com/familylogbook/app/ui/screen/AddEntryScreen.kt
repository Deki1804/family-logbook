package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.FeedingType
import com.familylogbook.app.ui.viewmodel.AddEntryViewModel
import kotlinx.coroutines.launch

@Composable
fun AddEntryScreen(
    viewModel: AddEntryViewModel,
    onNavigateBack: () -> Unit
) {
    val children by viewModel.children.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val entryText by viewModel.entryText.collectAsState()
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
            
            // Child selection
            Text(
                text = "Select Child (Optional)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Family option
                ChildChip(
                    label = "Family",
                    isSelected = selectedChildId == null,
                    onClick = { viewModel.setSelectedChild(null) },
                    modifier = Modifier.weight(1f)
                )
                
                // Children chips
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
            androidx.compose.foundation.BorderStroke(2.dp, color)
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

