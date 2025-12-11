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
import com.familylogbook.app.domain.model.Person
import com.familylogbook.app.domain.model.FeedingType

@Composable
fun QuickFeedingButtons(
    children: List<Person>, // List of CHILD type persons for feeding tracking
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
                text = "🍼 Brzo praćenje hranjenja",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (selectedChildId == null) {
                Text(
                    text = "Odaberi dijete za praćenje hranjenja",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                // Show selected person name
                val selectedPerson = children.find { it.id == selectedChildId }
                selectedPerson?.let { person ->
                    Text(
                        text = "Odabrano: ${person.emoji} ${person.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
                    Text("Zaustavi")
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

// Symptom options for health tracking
val COMMON_SYMPTOMS = listOf(
    "Temperatura" to "🌡️",
    "Kašalj" to "🤧",
    "Povraćanje" to "🤮",
    "Proljev" to "💩",
    "Osip" to "🔴",
    "Glavobolja" to "🤕",
    "Curenje nosa" to "🤧",
    "Bol u grlu" to "😷",
    "Umalaksalost" to "😴",
    "Gubitak apetita" to "🍽️",
    "Bol u trbuhu" to "😰",
    "Nesanica" to "😴"
)

@Composable
fun SymptomCheckboxSection(
    selectedSymptoms: Set<String>,
    onSymptomsChange: (Set<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🏥 Simptomi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Označi simptome koje primjećuješ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            // Grid of checkboxes (2 columns)
            val symptomsPerRow = 2
            COMMON_SYMPTOMS.chunked(symptomsPerRow).forEach { rowSymptoms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSymptoms.forEach { (symptom, emoji) ->
                        FilterChip(
                            selected = selectedSymptoms.contains(symptom),
                            onClick = {
                                val newSymptoms = if (selectedSymptoms.contains(symptom)) {
                                    selectedSymptoms - symptom
                                } else {
                                    selectedSymptoms + symptom
                                }
                                onSymptomsChange(newSymptoms)
                            },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                    Text(symptom, fontSize = 12.sp)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of items
                    if (rowSymptoms.size < symptomsPerRow) {
                        Spacer(modifier = Modifier.weight((symptomsPerRow - rowSymptoms.size).toFloat()))
                    }
                }
            }
        }
    }
}

