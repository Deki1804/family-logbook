package com.familylogbook.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shopping list card with checkboxes for shopping items.
 * Shows items as checkboxes - when checked, item is crossed out.
 * 
 * Note: Shopping deals are now shown via AdvicePill on HomeScreen using Google Custom Search API.
 */
@Composable
fun ShoppingListCard(
    items: List<String>,
    checkedItems: Set<String>,
    onItemChecked: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🛒 Lista namirnica",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            items.forEach { item ->
                ShoppingListItem(
                    item = item,
                    isChecked = checkedItems.contains(item),
                    onCheckedChange = { checked ->
                        onItemChecked(item, checked)
                    }
                )
            }
        }
    }
}

@Composable
fun ShoppingListItem(
    item: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        
        // Item text (crossed out if checked)
        Text(
            text = item,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            style = if (isChecked) {
                MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                MaterialTheme.typography.bodyMedium
            }
        )
    }
}
