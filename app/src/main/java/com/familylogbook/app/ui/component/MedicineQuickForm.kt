package com.familylogbook.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quick form for adding medicine entries.
 * Parent OS core feature.
 */
@Composable
fun MedicineQuickForm(
    medicineName: String,
    onMedicineNameChange: (String) -> Unit,
    medicineDosage: String,
    onMedicineDosageChange: (String) -> Unit,
    medicineIntervalHours: Int,
    onMedicineIntervalChange: (Int) -> Unit,
    onAddMedicine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = "Lijek",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Brzi unos lijeka",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Medicine name
            OutlinedTextField(
                value = medicineName,
                onValueChange = onMedicineNameChange,
                label = { Text("Naziv lijeka") },
                placeholder = { Text("npr. Nurofen, Paracetamol") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Dosage and interval row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = medicineDosage,
                    onValueChange = onMedicineDosageChange,
                    label = { Text("Doza") },
                    placeholder = { Text("5ml, 1 tableta") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                // Interval selector
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Interval",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(4, 6, 8, 12).forEach { hours ->
                            FilterChip(
                                selected = medicineIntervalHours == hours,
                                onClick = { onMedicineIntervalChange(hours) },
                                label = { Text("${hours}h", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Add button
            Button(
                onClick = onAddMedicine,
                modifier = Modifier.fillMaxWidth(),
                enabled = medicineName.isNotEmpty()
            ) {
                Text("Dodaj lijek")
            }
        }
    }
}
