package com.familylogbook.app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Standardized empty state component for consistent UX across the app.
 * Used when there's no data to display.
 */
@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    icon: String = "📋",
    title: String = "Još nema podataka",
    message: String = "Dodaj prvi zapis da počneš praćenje.",
    actionLabel: String? = "Dodaj zapis",
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = icon,
            fontSize = 64.sp
        )
        
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = message,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Empty state specifically for health entries (HomeScreen).
 */
@Composable
fun HealthEmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    EmptyState(
        modifier = modifier,
        icon = "🏥",
        title = "Još nema zdravstvenih zapisa",
        message = "Zabilježi prvi zdravstveni događaj da počneš praćenje zdravlja djece.",
        actionLabel = "Zabilježi zdravstveni događaj",
        onAction = onAddEntry
    )
}

/**
 * Empty state for day entries (DayTabScreen).
 */
@Composable
fun DayEmptyState(
    modifier: Modifier = Modifier,
    onAddEntry: () -> Unit
) {
    EmptyState(
        modifier = modifier,
        icon = "📅",
        title = "Nema dnevnih obaveza",
        message = "Dodaj rutine, checkliste ili podsjetnike da organiziraš dane.",
        actionLabel = "Dodaj obavezu",
        onAction = onAddEntry
    )
}

/**
 * Empty state for children (ChildTabScreen).
 */
@Composable
fun ChildrenEmptyState(
    modifier: Modifier = Modifier,
    onAddChild: () -> Unit
) {
    EmptyState(
        modifier = modifier,
        icon = "👶",
        title = "Još nema djece",
        message = "Dodaj dijete u postavkama da počneš praćenje.",
        actionLabel = "Dodaj dijete",
        onAction = onAddChild
    )
}

/**
 * Empty state for statistics (StatsScreen).
 */
@Composable
fun StatsEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        modifier = modifier,
        icon = "📊",
        title = "Još nema statistike",
        message = "Dodaj zdravstvene zapise da vidiš statistiku i uvid u zdravlje djece.",
        actionLabel = null,
        onAction = null
    )
}
