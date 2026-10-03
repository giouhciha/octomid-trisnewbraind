package com.octomid.trisbraind.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octomid.trisbraind.data.local.PredictionWithDraw
import com.octomid.trisbraind.domain.Turno
import com.octomid.trisbraind.domain.suggest.Strategy
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<PredictionWithDraw>,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pronósticos guardados", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Atrás", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("Borrar todo", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no hay pronósticos guardados.\n\nGenera sugerencias con el botón 2 y " +
                        "se guardarán aquí por fecha y sorteo.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val grouped = history.groupBy { it.prediction.fecha }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (fecha, items) ->
                    item(key = "date-$fecha") { DateHeader(fecha) }
                    items(items, key = { it.prediction.id }) { prediction ->
                        PredictionRow(prediction)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(epochDay: Long) {
    val date = LocalDate.ofEpochDay(epochDay)
    val label = if (date == LocalDate.now()) "$date · hoy" else date.toString()
    Text(
        label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun PredictionRow(item: PredictionWithDraw) {
    val prediction = item.prediction
    val winning = item.winningCombo
    val hit = item.hitCombo

    val statusText = when {
        winning == null -> "Pendiente"
        hit != null -> "ACERTÓ $hit"
        else -> "No acertó"
    }
    val statusColor = when {
        winning == null -> MaterialTheme.colorScheme.onSurfaceVariant
        hit != null -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val strategyLabel = runCatching { Strategy.valueOf(prediction.strategy).label }
        .getOrDefault(prediction.strategy)
    val turno = Turno.fromId(prediction.turno)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${turno?.hora ?: prediction.turno} · ${turno?.nombre ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(statusText, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                item.combos.joinToString("   "),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    "$strategyLabel · ${prediction.sampleSize} sorteos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                winning?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "salió $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
