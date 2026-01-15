package com.familylogbook.app.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Entity
import com.familylogbook.app.domain.model.EntityType
import com.familylogbook.app.domain.model.LogEntry
// SmartHomeManager import removed - no longer needed for Parent OS
import com.familylogbook.app.ui.component.StatItem
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntityProfileScreen(
    entityId: String,
    viewModel: HomeViewModel
) {
    val entities by viewModel.entities.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val persons by viewModel.persons.collectAsState()
    
    val entity = entities.find { it.id == entityId }
    val entityEntries = entries.filter { 
        it.entityId == entityId 
    }.sortedByDescending { it.timestamp }
    
    if (entity == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Entitet nije pronađen")
        }
        return
    }
    
    // Determine tabs based on entity type
    val tabs = when (entity.type) {
        EntityType.CAR -> listOf("Pregled", "Servisi", "Troškovi", "Povijest")
        EntityType.HOUSE -> listOf("Pregled", "Popravci", "Računi", "Održavanje")
        EntityType.FINANCE -> listOf("Pregled", "Troškovi", "Kategorije", "Sažetak")
        EntityType.SCHOOL -> listOf("Pregled", "Događaji", "Bilješke")
        EntityType.WORK -> listOf("Pregled", "Zadaci", "Bilješke")
        EntityType.SHOPPING -> listOf("Pregled", "Liste", "Povijest")
        EntityType.OTHER -> listOf("Pregled", "Povijest")
    }
    
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(android.graphics.Color.parseColor(entity.avatarColor)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = entity.emoji, fontSize = 24.sp)
                            }
                        }
                        Text(entity.name)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (entity.type) {
                    EntityType.CAR -> {
                        when (page) {
                            0 -> EntityOverviewTab(entity, entityEntries, viewModel)
                            1 -> CarServicesTab(entity, entityEntries, viewModel)
                            2 -> CarExpensesTab(entity, entityEntries, viewModel)
                            3 -> EntityHistoryTab(entity, entityEntries, viewModel)
                            else -> {}
                        }
                    }
                    EntityType.HOUSE -> {
                        when (page) {
                            0 -> EntityOverviewTab(entity, entityEntries, viewModel)
                            1 -> HouseRepairsTab(entity, entityEntries, viewModel)
                            2 -> HouseBillsTab(entity, entityEntries, viewModel)
                            3 -> HouseMaintenanceTab(entity, entityEntries, viewModel)
                            else -> {}
                        }
                    }
                    EntityType.FINANCE -> {
                        when (page) {
                            0 -> EntityOverviewTab(entity, entityEntries, viewModel)
                            1 -> FinanceExpensesTab(entity, entityEntries, viewModel)
                            2 -> FinanceCategoriesTab(entity, entityEntries, viewModel)
                            3 -> FinanceSummaryTab(entity, entityEntries, viewModel)
                            else -> {}
                        }
                    }
                    else -> {
                        when (page) {
                            0 -> EntityOverviewTab(entity, entityEntries, viewModel)
                            else -> EntityHistoryTab(entity, entityEntries, viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Overview Tab (common for all entities)
@Composable
fun EntityOverviewTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val persons by viewModel.persons.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats summary
        item {
            EntityStatsSummary(entity, entries)
        }
        
        // Recent entries
        item {
            Text(
                text = "Nedavni zapisi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (entries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Još nema zapisa za ${entity.name}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            }
        } else {
            items(entries.take(10)) { entry ->
                val person = entry.personId?.let { personId ->
                    persons.find { it.id == personId }
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                LogEntryCard(
                    entry = entry,
                    person = person,
                    entity = entity,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun EntityStatsSummary(entity: Entity, entries: List<LogEntry>) {
    val totalEntries = entries.size
    val autoEntries = entries.count { it.category == Category.AUTO }
    val financeEntries = entries.count { it.category == Category.FINANCE }
    val houseEntries = entries.count { it.category == Category.HOUSE }
    
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
                text = "Sažetak",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Ukupno",
                    count = totalEntries,
                    color = MaterialTheme.colorScheme.primary
                )
                
                when (entity.type) {
                    EntityType.CAR -> {
                        StatItem(
                            label = "Servisi",
                            count = autoEntries,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    EntityType.HOUSE -> {
                        StatItem(
                            label = "Popravci",
                            count = houseEntries,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    EntityType.FINANCE -> {
                        StatItem(
                            label = "Transakcije",
                            count = financeEntries,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

// Car-specific tabs
@Composable
fun CarServicesTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val serviceEntries = entries.filter { 
        it.category == Category.AUTO && it.serviceType != null 
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Povijest servisa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (serviceEntries.isEmpty()) {
            item {
                Text(
                    text = "Još nema servisnih zapisa",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(serviceEntries) { entry ->
                ServiceCard(entry)
            }
        }
    }
}

@Composable
fun CarExpensesTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val expenseEntries = entries.filter { 
        it.category == Category.FINANCE && it.amount != null && it.entityId == entity.id
    }
    val totalExpenses = expenseEntries.sumOf { it.amount ?: 0.0 }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ukupni troškovi",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%.2f", totalExpenses)} ${expenseEntries.firstOrNull()?.currency ?: "EUR"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Povijest troškova",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (expenseEntries.isEmpty()) {
            item {
                Text(
                    text = "Još nema zabilježenih troškova",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(expenseEntries) { entry ->
                ExpenseCard(entry)
            }
        }
    }
}

// House-specific tabs
@Composable
fun HouseRepairsTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val repairEntries = entries.filter { 
        it.category == Category.HOUSE 
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Popravci i održavanje",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (repairEntries.isEmpty()) {
            item {
                Text(
                    text = "Još nema zapisa o popravcima",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(repairEntries) { entry ->
                RepairCard(entry)
            }
        }
    }
}

@Composable
fun HouseBillsTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val billEntries = entries.filter { 
        it.category == Category.FINANCE && it.reminderDate != null
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Računi i plaćanja",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (billEntries.isEmpty()) {
            item {
                Text(
                    text = "Još nema zabilježenih računa",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(billEntries) { entry ->
                BillCard(entry)
            }
        }
    }
}

@Composable
fun HouseMaintenanceTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    // Placeholder for maintenance tracking
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Raspored održavanja",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Text(
                text = "Uskoro...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }
    }
}

// Finance-specific tabs
@Composable
fun FinanceExpensesTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val expenseEntries = entries.filter { 
        it.category == Category.FINANCE && it.amount != null
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Svi troškovi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (expenseEntries.isEmpty()) {
            item {
                Text(
                    text = "Još nema zabilježenih troškova",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(expenseEntries) { entry ->
                ExpenseCard(entry)
            }
        }
    }
}

@Composable
fun FinanceCategoriesTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val expenseEntries = entries.filter { 
        it.category == Category.FINANCE && it.amount != null
    }
    
    // Group by tags/categories
    val categoryGroups = expenseEntries.groupBy { 
        it.tags.firstOrNull() ?: "Ostalo"
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Troškovi po kategorijama",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (categoryGroups.isEmpty()) {
            item {
                Text(
                    text = "Još nema kategoriziranih troškova",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            categoryGroups.forEach { (category, entries) ->
                item {
                    val total = entries.sumOf { it.amount ?: 0.0 }
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format("%.2f", total)} ${entries.firstOrNull()?.currency ?: "EUR"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceSummaryTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val expenseEntries = entries.filter { 
        it.category == Category.FINANCE && it.amount != null
    }
    val totalExpenses = expenseEntries.sumOf { it.amount ?: 0.0 }
    val monthlyExpenses = expenseEntries
        .groupBy { 
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
        }
        .mapValues { (_, entries) -> entries.sumOf { it.amount ?: 0.0 } }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ukupni troškovi",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${String.format("%.2f", totalExpenses)} ${expenseEntries.firstOrNull()?.currency ?: "EUR"}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Mjesečni pregled",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (monthlyExpenses.isEmpty()) {
            item {
                Text(
                    text = "Još nema mjesečnih podataka",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            // Monthly expense chart
            item {
                val sortedMonths = monthlyExpenses.toList().sortedBy { it.first }
                val maxAmount = sortedMonths.maxOfOrNull { it.second } ?: 1.0
                SimpleExpenseChart(
                    monthlyData = sortedMonths,
                    maxAmount = maxAmount,
                    currency = expenseEntries.firstOrNull()?.currency ?: "EUR"
                )
            }
            
            // Monthly breakdown list
            monthlyExpenses.toList().sortedByDescending { it.first }.forEach { (month, total) ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = month,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format("%.2f", total)} ${expenseEntries.firstOrNull()?.currency ?: "EUR"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Common History Tab
@Composable
fun EntityHistoryTab(
    entity: Entity,
    entries: List<LogEntry>,
    viewModel: HomeViewModel
) {
    val persons by viewModel.persons.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Svi zapisi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (entries.isEmpty()) {
            item {
                Text(
                    text = "Još nema zapisa",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(entries) { entry ->
                val person = entry.personId?.let { personId ->
                    persons.find { it.id == personId }
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                LogEntryCard(
                    entry = entry,
                    person = person,
                    entity = entity,
                    viewModel = viewModel
                )
            }
        }
    }
}

// Helper card composables
@Composable
fun ServiceCard(entry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.serviceType ?: "Servis",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.timestamp)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            entry.mileage?.let {
                Text(
                    text = "Kilometraža: $it km",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ExpenseCard(entry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.rawText.take(50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            entry.amount?.let {
                Text(
                    text = "${String.format("%.2f", it)} ${entry.currency ?: "EUR"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RepairCard(entry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.rawText.take(100),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.timestamp)),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            entry.amount?.let {
                Text(
                    text = "Cijena: ${String.format("%.2f", it)} ${entry.currency ?: "EUR"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BillCard(entry: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.rawText.take(50),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                entry.reminderDate?.let {
                    Text(
                        text = "Dospijeće: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            entry.amount?.let {
                Text(
                    text = "${String.format("%.2f", it)} ${entry.currency ?: "EUR"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Simple expense chart component
@Composable
fun SimpleExpenseChart(
    monthlyData: List<Pair<String, Double>>,
    maxAmount: Double,
    currency: String
) {
    if (monthlyData.isEmpty()) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            monthlyData.forEach { (month, amount) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = month,
                        fontSize = 12.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(
                                        ((amount / maxAmount * 100).dp.coerceAtLeast(4.dp))
                                    )
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Text(
                            text = "${String.format("%.0f", amount)} $currency",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
