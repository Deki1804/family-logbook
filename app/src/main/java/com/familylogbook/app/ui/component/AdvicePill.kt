package com.familylogbook.app.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.AdviceTemplate

/**
 * Compact "pill" style advice notification.
 * Shows minimal advice info with "Pogledaj detalje" action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdvicePill(
    advice: AdviceTemplate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getAdviceIcon(advice.id),
                    fontSize = 16.sp
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = advice.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = advice.shortDescription.take(50) + if (advice.shortDescription.length > 50) "..." else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
            
            TextButton(
                onClick = onClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Pogledaj",
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun getAdviceIcon(adviceId: String): String {
    return when {
        adviceId.contains("shopping") || adviceId.contains("deal") -> "💰"
        adviceId.contains("vaccination") || adviceId.contains("cjepivo") -> "💉"
        adviceId.contains("feeding") || adviceId.contains("hranjenje") -> "🍼"
        adviceId.contains("health") || adviceId.contains("zdravlje") -> "🏥"
        adviceId.contains("smart") || adviceId.contains("home") -> "🏠"
        else -> "💡"
    }
}
