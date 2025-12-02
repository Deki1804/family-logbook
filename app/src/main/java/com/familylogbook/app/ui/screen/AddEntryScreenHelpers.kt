package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Child
import com.familylogbook.app.domain.model.FeedingType

@Composable
fun QuickFeedingButtons(
    children: List<Child>,
    selectedChildId: String?,
    onSelectChild: (String?) -> Unit,
    onStartFeeding: (FeedingType) -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🍼 Quick Feeding Tracker",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (selectedChildId == null) {
                Text(
                    text = "Odaberi dijete za praćenje hranjenja",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onStartFeeding(FeedingType.BREAST_LEFT) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("👈 Lijeva", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { onStartFeeding(FeedingType.BREAST_RIGHT) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("👉 Desna", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { onStartFeeding(FeedingType.BOTTLE) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🍼 Bočica", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FeedingTimerCard(
    elapsedSeconds: Long,
    feedingType: FeedingType?,
    bottleAmount: String,
    onBottleAmountChange: (String) -> Unit,
    onStop: () -> Unit,
    onSave: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (feedingType) {
                    FeedingType.BREAST_LEFT -> "👈 Dojenje (lijeva)"
                    FeedingType.BREAST_RIGHT -> "👉 Dojenje (desna)"
                    FeedingType.BOTTLE -> "🍼 Bočica"
                    null -> "🍼 Hranjenje"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (feedingType == FeedingType.BOTTLE) {
                OutlinedTextField(
                    value = bottleAmount,
                    onValueChange = onBottleAmountChange,
                    label = { Text("Količina (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop")
                }
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Spremi")
                }
            }
        }
    }
}

