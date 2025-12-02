package com.familylogbook.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    viewModel: StatsViewModel
) {
    val categoryCounts by viewModel.categoryCounts.collectAsState()
    val moodCounts by viewModel.moodCounts.collectAsState()
    
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
        
        // Category stats
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
                CategoryStatRow(categoryCount = categoryCount)
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
    }
}

@Composable
fun CategoryStatRow(categoryCount: CategoryCount) {
    val (label, color) = when (categoryCount.category) {
        Category.HEALTH -> "Health" to Color(0xFFFF6B6B)
        Category.SLEEP -> "Sleep" to Color(0xFF4ECDC4)
        Category.MOOD -> "Mood" to Color(0xFFFFD93D)
        Category.DEVELOPMENT -> "Development" to Color(0xFF95E1D3)
        Category.KINDERGARTEN_SCHOOL -> "School" to Color(0xFFAA96DA)
        Category.HOME -> "Home" to Color(0xFFF38181)
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
        Mood.VERY_BAD -> "Very Bad" to "😢" to Color(0xFFE63946)
        Mood.BAD -> "Bad" to "😞" to Color(0xFFFF6B6B)
        Mood.NEUTRAL -> "Neutral" to "😐" to Color(0xFFCCCCCC)
        Mood.GOOD -> "Good" to "😊" to Color(0xFF4ECDC4)
        Mood.VERY_GOOD -> "Very Good" to "😄" to Color(0xFF2A9D8F)
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

