package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.Mood
import com.familylogbook.app.ui.viewmodel.CategoryCount
import com.familylogbook.app.ui.viewmodel.MoodCount
import com.familylogbook.app.ui.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onCategoryClick: (Category) -> Unit = {}
) {
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val moodCounts by viewModel.moodCounts.collectAsState()
    val temperatureHistory by viewModel.temperatureHistory.collectAsState()
    val feedingHistory by viewModel.feedingHistory.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Statistics",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Category stats (clickable)
        Text(
            text = "Entries by Category",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (categoryCounts.isEmpty()) {
            Text(
                text = "No entries yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            categoryCounts.forEach { categoryCount ->
                CategoryStatRow(
                    categoryCount = categoryCount,
                    onClick = { onCategoryClick(categoryCount.category) }
                )
            }
        }
        
        Divider()
        
        // Mood stats
        Text(
            text = "Mood Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (moodCounts.isEmpty()) {
            Text(
                text = "No mood data yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            moodCounts.forEach { moodCount ->
                MoodStatRow(moodCount = moodCount)
            }
        }
        
        Divider()
        
        // Temperature History
        Text(
            text = "Temperature History (Last 7 Days)",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (temperatureHistory.isEmpty()) {
            Text(
                text = "No temperature data yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            SimpleTemperatureChart(temperatureHistory = temperatureHistory)
        }
        
        Divider()
        
        // Feeding History
        Text(
            text = "Feeding History (Last 7 Days)",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (feedingHistory.isEmpty()) {
            Text(
                text = "No feeding data yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            SimpleFeedingChart(feedingHistory = feedingHistory)
        }
    }
}

@Composable
fun SimpleTemperatureChart(temperatureHistory: List<Pair<Long, Float>>) {
    if (temperatureHistory.isEmpty()) return
    
    val maxTemp = temperatureHistory.maxOfOrNull { it.second } ?: 40f
    val minTemp = temperatureHistory.minOfOrNull { it.second } ?: 36f
    val tempRange = maxTemp - minTemp
    
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
            temperatureHistory.forEach { (timestamp, temp) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(timestamp)),
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
                                        ((temp - minTemp) / tempRange * 100).dp.coerceAtLeast(4.dp)
                                    )
                                    .background(
                                        if (temp >= 38f) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Text(
                            text = "${temp}°C",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleFeedingChart(feedingHistory: List<Pair<Long, Int>>) {
    if (feedingHistory.isEmpty()) return
    
    val maxAmount = feedingHistory.maxOfOrNull { it.second } ?: 200
    
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
            feedingHistory.forEach { (timestamp, amount) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(timestamp)),
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
                                        ((amount.toFloat() / maxAmount * 100).dp.coerceAtLeast(4.dp))
                                    )
                                    .background(
                                        MaterialTheme.colorScheme.tertiary,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        Text(
                            text = "${amount}ml",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryStatRow(
    categoryCount: CategoryCount,
    onClick: () -> Unit = {}
) {
    val (label, color) = when (categoryCount.category) {
        Category.HEALTH -> "Health" to Color(0xFFFF6B6B)
        Category.SLEEP -> "Sleep" to Color(0xFF4ECDC4)
        Category.MOOD -> "Mood" to Color(0xFFFFD93D)
        Category.DEVELOPMENT -> "Development" to Color(0xFF95E1D3)
        Category.KINDERGARTEN_SCHOOL -> "School" to Color(0xFFAA96DA)
        Category.SCHOOL -> "School" to Color(0xFFAA96DA)
        Category.HOME -> "Home" to Color(0xFFF38181)
        Category.HOUSE -> "House" to Color(0xFFF38181)
        Category.FEEDING -> "Feeding" to Color(0xFFFFB84D)
        Category.AUTO -> "Auto" to Color(0xFFFF6B6B)
        Category.FINANCE -> "Finance" to Color(0xFF95E1D3)
        Category.WORK -> "Work" to Color(0xFFAA96DA)
        Category.SHOPPING -> "Shopping" to Color(0xFFFFB84D)
        Category.SMART_HOME -> "Smart Home" to Color(0xFF00BCD4)
        Category.OTHER -> "Other" to Color(0xFFCCCCCC)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label.first().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${categoryCount.count}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun MoodStatRow(moodCount: MoodCount) {
    val (label, emoji, color) = when (moodCount.mood) {
        Mood.VERY_BAD -> Triple("Very Bad", "😢", Color(0xFFE63946))
        Mood.BAD -> Triple("Bad", "😞", Color(0xFFFF6B6B))
        Mood.NEUTRAL -> Triple("Neutral", "😐", Color(0xFFCCCCCC))
        Mood.GOOD -> Triple("Good", "😊", Color(0xFF4ECDC4))
        Mood.VERY_GOOD -> Triple("Very Good", "😄", Color(0xFF2A9D8F))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${moodCount.count}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

