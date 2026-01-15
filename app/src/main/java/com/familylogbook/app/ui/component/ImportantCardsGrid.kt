package com.familylogbook.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.domain.model.LogEntry
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.Entity

/**
 * Grid of important cards (2x2 layout).
 * Parent OS: 4 core shortcuts (Djeca, Zdravlje, Dnevne obaveze, Savjeti).
 */
@Composable
fun ImportantCardsGrid(
    persons: List<Person>,
    entries: List<LogEntry>,
    onChildClick: () -> Unit = {},
    onHealthClick: () -> Unit = {},
    onDayClick: () -> Unit = {},
    onAdviceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Calculate stats
    val children = persons.filter { it.type == com.familylogbook.app.domain.model.PersonType.CHILD }
    
    // Parent OS categories
    val healthEntries = entries.filter { 
        it.category == Category.HEALTH || 
        it.category == Category.MEDICINE || 
        it.category == Category.SYMPTOM || 
        it.category == Category.VACCINATION 
    }
    val dayEntries = entries.filter { it.category == Category.DAY }
    
    val adviceCount = entries.count { it.category != Category.OTHER }
    
    // Build list of cards to show (only relevant ones)
    val cardsToShow = mutableListOf<CardInfo>()
    
    // Child card (always present; if no children, acts as a clear call-to-action)
    cardsToShow.add(
        CardInfo(
            title = "👶 Djeca",
            subtitle = if (children.isEmpty()) {
                "Dodaj dijete"
            } else {
                "${children.size} ${if (children.size == 1) "dijete" else "djece"}"
            },
            badge = if (children.isNotEmpty()) "${children.size}" else null,
            onClick = onChildClick,
            iconColor = Color(0xFF4ECDC4)
        )
    )
    
    // Health card - Parent OS core feature
    cardsToShow.add(CardInfo(
        title = "🏥 Zdravlje",
        subtitle = if (healthEntries.isEmpty()) "Nema zdravstvenih zapisa" else "${healthEntries.size} zdravstvenih zapisa",
        badge = if (healthEntries.isNotEmpty()) "${healthEntries.size}" else null,
        onClick = onHealthClick,
        iconColor = Color(0xFFFF6B6B)
    ))
    
    // Day routines card - Parent OS core feature
    cardsToShow.add(CardInfo(
        title = "📋 Dnevne obaveze",
        subtitle = if (dayEntries.isEmpty()) "Nema rutina" else "${dayEntries.size} rutina",
        badge = if (dayEntries.isNotEmpty()) "${dayEntries.size}" else null,
        onClick = onDayClick,
        iconColor = Color(0xFF6C5CE7)
    ))
    
    // Advice card - always show
    cardsToShow.add(CardInfo(
        title = "💡 Savjeti",
        subtitle = if (adviceCount == 0) "Nema savjeta" else "$adviceCount preporuka",
        badge = if (adviceCount > 0) "$adviceCount" else null,
        onClick = onAdviceClick,
        iconColor = Color(0xFFFFD93D)
    ))
    
    // Display cards in 2x2 grid
    val cardsInRow1 = cardsToShow.take(2)
    val cardsInRow2 = cardsToShow.drop(2).take(2)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1
        if (cardsInRow1.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cardsInRow1.forEach { cardInfo ->
                    ImportantCard(
                        title = cardInfo.title,
                        subtitle = cardInfo.subtitle,
                        badge = cardInfo.badge,
                        onClick = cardInfo.onClick,
                        modifier = Modifier.weight(1f),
                        iconColor = cardInfo.iconColor
                    )
                }
                // Fill empty space if only 1 card in row
                if (cardsInRow1.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Row 2
        if (cardsInRow2.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cardsInRow2.forEach { cardInfo ->
                    ImportantCard(
                        title = cardInfo.title,
                        subtitle = cardInfo.subtitle,
                        badge = cardInfo.badge,
                        onClick = cardInfo.onClick,
                        modifier = Modifier.weight(1f),
                        iconColor = cardInfo.iconColor
                    )
                }
                // Fill empty space if only 1 card in row
                if (cardsInRow2.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class CardInfo(
    val title: String,
    val subtitle: String,
    val badge: String?,
    val onClick: () -> Unit,
    val iconColor: Color
)

@Composable
private fun ImportantCard(
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .then(Modifier.clickable { onClick() }),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Badge
            badge?.let {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = it,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
