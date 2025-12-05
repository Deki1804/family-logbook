package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.data.smarthome.SmartHomeManager
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.ui.component.AdviceCard
import com.familylogbook.app.ui.component.StatItem
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChildProfileScreen(
    childId: String,
    viewModel: HomeViewModel
) {
    val children by viewModel.children.collectAsState()
    val entries by viewModel.entries.collectAsState()
    
    val child = children.find { it.id == childId }
    val childEntries = entries.filter { it.childId == childId }.sortedByDescending { it.timestamp }
    
    if (child == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Dijete nije pronađeno")
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(android.graphics.Color.parseColor(child.avatarColor)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                    Text(text = child.emoji, fontSize = 24.sp)
                            }
                        }
                        Text(child.name)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats summary
            item {
                ChildStatsSummary(childEntries = childEntries)
            }
            
            // Entries
            items(childEntries) { entry ->
                val person = entry.personId?.let { personId ->
                    viewModel.getPersonById(personId)
                } ?: child?.let {
                    Person(it.id, it.name, emoji = it.emoji, avatarColor = it.avatarColor)
                }
                val entity = entry.entityId?.let { entityId ->
                    viewModel.getEntityById(entityId)
                }
                val context = LocalContext.current
                val smartHomeManager = remember(context) { SmartHomeManager(context) }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogEntryCard(
                        entry = entry,
                        person = person,
                        entity = entity,
                        viewModel = viewModel,
                        smartHomeManager = smartHomeManager
                    )
                }
            }
            
            if (childEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No entries yet for ${child.name}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChildStatsSummary(childEntries: List<LogEntry>) {
    val healthCount = childEntries.count { it.category == com.familylogbook.app.domain.model.Category.HEALTH }
    val feedingCount = childEntries.count { it.category == com.familylogbook.app.domain.model.Category.FEEDING }
    val sleepCount = childEntries.count { it.category == com.familylogbook.app.domain.model.Category.SLEEP }
    val developmentCount = childEntries.count { it.category == com.familylogbook.app.domain.model.Category.DEVELOPMENT }
    
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
            Text(
                text = "Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Zdravlje", healthCount, Color(0xFFFF6B6B))
                StatItem("Hranjenje", feedingCount, Color(0xFFFFB84D))
                StatItem("Spavanje", sleepCount, Color(0xFF4ECDC4))
                StatItem("Razvoj", developmentCount, Color(0xFF95E1D3))
            }
        }
    }
}

