package com.familylogbook.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familylogbook.app.domain.model.Category
import com.familylogbook.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Read-only detalji zapisa s brzim gumbom za uređivanje.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryDetailScreen(
    entryId: String,
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val entities by viewModel.entities.collectAsState()
    val scope = rememberCoroutineScope()

    val entry = entries.find { it.id == entryId }

    if (entry == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Zdravstveni zapis nije pronađen") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Natrag")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Zdravstveni zapis nije pronađen")
            }
        }
        return
    }

    val person = entry.personId?.let { persons.find { p -> p.id == it } }
    val entity = entry.entityId?.let { entities.find { e -> e.id == it } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalji zdravstvenog zapisa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Natrag")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(entry.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Uredi")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            person?.let {
                PersonEntityInfoCard(
                    title = "Osoba",
                    name = it.name,
                    emoji = it.emoji,
                    avatarColor = it.avatarColor
                )
            } ?: entity?.let {
                PersonEntityInfoCard(
                    title = "Entitet",
                    name = it.name,
                    emoji = it.emoji,
                    avatarColor = it.avatarColor
                )
            }

            InfoCard(label = "Kategorija", value = getCategoryDisplayName(entry.category))

            InfoCard(
                label = "Datum i vrijeme",
                value = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(Date(entry.timestamp))
            )

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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tekst",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = entry.rawText,
                        fontSize = 16.sp
                    )
                }
            }

            if (entry.tags.isNotEmpty()) {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Oznake",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            entry.tags.forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when (entry.category) {
                Category.HEALTH -> {
                    entry.temperature?.let { InfoCard("Temperatura", "${it}°C") }
                    entry.medicineGiven?.let { InfoCard("Lijek", it) }
                    entry.medicineIntervalHours?.let { InfoCard("Interval lijeka", "${it} sati") }
                }
                Category.FEEDING -> {
                    entry.feedingType?.let { InfoCard("Tip hranjenja", it.name) }
                    entry.feedingAmount?.let { InfoCard("Količina", "${it} ml") }
                }
                Category.FINANCE -> {
                    entry.amount?.let {
                        InfoCard("Iznos", "${String.format("%.2f", it)} ${entry.currency ?: "EUR"}")
                    }
                    entry.reminderDate?.let {
                        InfoCard(
                            "Dospijeće",
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                        )
                    }
                }
                Category.MEDICINE -> {
                    entry.medicineGiven?.let { InfoCard("Lijek", it) }
                    entry.medicineDosage?.let { InfoCard("Doza", it) }
                    entry.medicineIntervalHours?.let { InfoCard("Interval", "${it} sati") }
                    entry.nextMedicineTime?.let {
                        InfoCard(
                            "Sljedeća doza",
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))
                        )
                    }
                }
                Category.SYMPTOM -> {
                    entry.temperature?.let { InfoCard("Temperatura", "${it}°C") }
                    entry.symptoms?.takeIf { it.isNotEmpty() }?.let {
                        InfoCard("Simptomi", it.joinToString(", "))
                    }
                }
                Category.VACCINATION -> {
                    entry.vaccinationName?.let { InfoCard("Cjepivo", it) }
                    entry.vaccinationDate?.let { date ->
                        InfoCard(
                            "Datum",
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(date))
                        )
                    }
                }
                Category.DAY -> {
                    entry.dayEntryType?.let { type ->
                        val typeName = when (type) {
                            com.familylogbook.app.domain.model.DayEntryType.TODAY -> "Dnevni zapis"
                            com.familylogbook.app.domain.model.DayEntryType.CHECKLIST -> "Checklist"
                            com.familylogbook.app.domain.model.DayEntryType.REMINDER -> "Podsjetnik"
                        }
                        InfoCard("Tip", typeName)
                    }
                    entry.isCompleted?.let { completed ->
                        InfoCard("Završeno", if (completed) "Da" else "Ne")
                    }
                    entry.dueDate?.let { date ->
                        InfoCard(
                            "Rok",
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(date))
                        )
                    }
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onEditClick(entry.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uredi zdravstveni zapis")
            }
        }
    }
}

@Composable
private fun PersonEntityInfoCard(
    title: String,
    name: String,
    emoji: String,
    avatarColor: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(android.graphics.Color.parseColor(avatarColor)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
