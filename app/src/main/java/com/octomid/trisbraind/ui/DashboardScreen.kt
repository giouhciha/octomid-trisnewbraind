package com.octomid.trisbraind.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octomid.trisbraind.domain.suggest.Strategy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tris Brain", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatusCard(state) }
            item {
                ActionsRow(
                    state = state,
                    onSync = viewModel::sync,
                    onSuggest = viewModel::suggest,
                    onHistory = viewModel::showHistory,
                    onBias = viewModel::showBias
                )
            }
            item {
                SettingsCard(
                    state = state,
                    onCombos = viewModel::setCombosPerTurno,
                    onStrategy = viewModel::setStrategy
                )
            }
            item { DisclaimerCard() }
            items(state.turnos) { turnoView -> TurnoCard(turnoView) }
        }
    }
}

@Composable
private fun StatusCard(state: MainUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Base de datos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${state.totalDraws} concursos guardados" +
                    (state.lastFecha?.let { " · último: $it" } ?: ""),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            state.message?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    color = if (state.isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActionsRow(
    state: MainUiState,
    onSync: () -> Unit,
    onSuggest: () -> Unit,
    onHistory: () -> Unit,
    onBias: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onSync,
            enabled = !state.syncing && !state.generating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("1 · Actualizar resultados")
        }
        Button(
            onClick = onSuggest,
            enabled = !state.syncing && !state.generating && state.totalDraws > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.generating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("2 · Sugerir combinaciones para hoy")
        }
        OutlinedButton(
            onClick = onHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.historyCount > 0) "Ver pronósticos guardados (${state.historyCount})"
                else "Ver pronósticos guardados"
            )
        }
        OutlinedButton(
            onClick = onBias,
            enabled = state.totalDraws > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Monitor de sesgo")
        }
    }
}

@Composable
private fun SettingsCard(
    state: MainUiState,
    onCombos: (Int) -> Unit,
    onStrategy: (Strategy) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Ajustes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Combinaciones por sorteo: ", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { onCombos(state.combosPerTurno - 1) }) { Text("−") }
                Text(
                    "  ${state.combosPerTurno}  ",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedButton(onClick = { onCombos(state.combosPerTurno + 1) }) { Text("+") }
            }
            Spacer(Modifier.height(12.dp))
            Text("Estrategia", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Strategy.entries.forEach { strategy ->
                    FilterChip(
                        selected = state.strategy == strategy,
                        onClick = { onStrategy(strategy) },
                        label = { Text(strategy.label) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                state.strategy.detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            "Cada dígito sale de una urna independiente (0–9). La probabilidad base es 1/10 " +
                "por dígito y 1/100,000 por combinación exacta, sin importar la combinación " +
                "elegida. Estas sugerencias no superan al azar: sirven como cobertura y como " +
                "monitoreo de posibles sesgos.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TurnoCard(view: TurnoView) {
    val suggestion = view.suggestion
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        suggestion.turno.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${suggestion.turno.hora} · ${suggestion.sampleSize} sorteos analizados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            suggestion.combos.forEachIndexed { index, combo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MediumBadge(index + 1)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        combo,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (index != suggestion.combos.lastIndex) Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            SignalLine(view)
        }
    }
}

@Composable
private fun MediumBadge(number: Int) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "#$number",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun SignalLine(view: TurnoView) {
    val suggestion = view.suggestion
    val backtest = view.backtest
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (suggestion.deviantPositions.isEmpty()) {
            Text(
                "Sin desviación significativa al 5% (urnas uniformes).",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Posición con posible desviación: ${suggestion.deviantPositions.joinToString(", ")}.",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.error
            )
        }
        Text(
            "Backtest dígito-por-dígito: aciertos ${pct(backtest.top1Accuracy)} " +
                "(azar ${pct(backtest.uniformAccuracy)}) · log-loss " +
                "${"%.3f".format(backtest.logLossPerDigit)} (azar ${"%.3f".format(backtest.uniformLogLoss)}).",
            style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}

private fun pct(value: Double): String = "%.1f%%".format(value * 100.0)

@Composable
private fun Divider() {
    androidx.compose.material3.HorizontalDivider()
}
