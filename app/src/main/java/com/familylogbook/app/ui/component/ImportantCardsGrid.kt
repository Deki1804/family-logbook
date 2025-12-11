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
 * Shows: Child, Shopping, Advice, Smart Home
 */
@Composable
fun ImportantCardsGrid(
    persons: List<Person>,
    entities: List<Entity>,
    entries: List<LogEntry>,
    onChildClick: () -> Unit = {},
    onEntityClick: (String) -> Unit = {}, // entityId
    onShoppingClick: () -> Unit = {},
    onAdviceClick: () -> Unit = {},
    onSmartHomeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Calculate stats
    val children = persons.filter { it.type == com.familylogbook.app.domain.model.PersonType.CHILD }
    val shoppingEntries = entries.filter { it.category == Category.SHOPPING }
    val uncheckedShoppingItems = shoppingEntries
        .flatMap { it.shoppingItems ?: emptyList() }
        .filter { item -> 
            shoppingEntries.none { entry -> 
                entry.checkedShoppingItems?.contains(item) == true 
            }
        }
        .distinct()
    
    val adviceCount = entries.count { 
        it.category != Category.OTHER && 
        it.category != Category.SHOPPING 
    }
    
    // Build list of cards to show (only relevant ones)
    val cardsToShow = mutableListOf<CardInfo>()
    
    // Child card - only if user has children
    if (children.isNotEmpty()) {
        cardsToShow.add(CardInfo(
            title = "👶 Djeca",
            subtitle = "${children.size} ${if (children.size == 1) "dijete" else "djece"}",
            badge = null,
            onClick = onChildClick,
            iconColor = Color(0xFF4ECDC4)
        ))
    }
    
    // Entity cards - only if user has entities
    entities.forEach { entity ->
        val entityIcon = when (entity.type) {
            com.familylogbook.app.domain.model.EntityType.HOUSE -> "🏡"
            com.familylogbook.app.domain.model.EntityType.CAR -> "🚗"
            com.familylogbook.app.domain.model.EntityType.FINANCE -> "💰"
            com.familylogbook.app.domain.model.EntityType.SCHOOL -> "🏫"
            com.familylogbook.app.domain.model.EntityType.WORK -> "💼"
            com.familylogbook.app.domain.model.EntityType.SHOPPING -> "🛒"
            else -> entity.emoji.ifEmpty { "📦" }
        }
        cardsToShow.add(CardInfo(
            title = "$entityIcon ${entity.name}",
            subtitle = "Detalji",
            badge = null,
            onClick = { onEntityClick(entity.id) },
            iconColor = Color(0xFFAA96DA)
        ))
    }
    
    // Shopping card - always show
    cardsToShow.add(CardInfo(
        title = "🛒 Shopping",
        subtitle = if (uncheckedShoppingItems.isEmpty()) "Lista prazna" else "${uncheckedShoppingItems.size} stavki",
        badge = if (uncheckedShoppingItems.isNotEmpty()) "${uncheckedShoppingItems.size}" else null,
        onClick = onShoppingClick,
        iconColor = Color(0xFFFF6B6B)
    ))
    
    // Advice card - always show
    cardsToShow.add(CardInfo(
        title = "💡 Savjeti",
        subtitle = if (adviceCount == 0) "Nema savjeta" else "$adviceCount preporuka",
        badge = if (adviceCount > 0) "$adviceCount" else null,
        onClick = onAdviceClick,
        iconColor = Color(0xFFFFD93D)
    ))
    
    // Smart Home card - always show
    cardsToShow.add(CardInfo(
        title = "🏠 Smart Home",
        subtitle = "Upravljanje",
        badge = null,
        onClick = onSmartHomeClick,
        iconColor = Color(0xFF95E1D3)
    ))
    
    // Display cards in 2x2 grid (or adjust based on number of cards)
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
