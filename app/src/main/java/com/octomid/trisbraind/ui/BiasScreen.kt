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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octomid.trisbraind.domain.stats.BiasMonitor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiasScreen(
    report: BiasMonitor.Report?,
    window: Int,
    loading: Boolean,
    onBack: () -> Unit,
    onWindow: (Int) -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor de sesgo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Atrás", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Ventana de análisis",
                style = MaterialTheme.typography.labelLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BiasMonitor.windows.forEach { option ->
                    FilterChip(
                        selected = window == option,
                        onClick = { onWindow(option) },
                        label = { Text(if (option == 0) "Todo" else option.toString()) }
                    )
                }
            }

            if (loading || report == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SummaryCard(report)
                report.turnos.forEach { turnoResult ->
                    TurnoBiasCard(turnoResult, report.correctedAlpha)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(report: BiasMonitor.Report) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (report.anySignificant) {
                    "Se detectaron ${report.significantCount} urna(s) con desviación sostenida."
                } else {
                    "Sin sesgo sostenido en ninguna urna."
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${report.totalTests} pruebas (5 turnos × 5 posiciones) · " +
                    "umbral corregido p < ${formatP(report.correctedAlpha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Con la corrección de Bonferroni, los falsos positivos esperados " +
                    "vuelven a ser ~${"%.2f".format(report.expectedFalsePositives)}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TurnoBiasCard(turnoResult: BiasMonitor.TurnoResult, correctedAlpha: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${turnoResult.turno.hora} · ${turnoResult.turno.nombre}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${turnoResult.sampleSize} sorteos en la ventana",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            turnoResult.positions.forEachIndexed { index, position ->
                PositionRow(position, correctedAlpha)
                if (index != turnoResult.positions.lastIndex) {
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun PositionRow(position: BiasMonitor.PositionResult, correctedAlpha: Double) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "R${position.position}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(34.dp)
            )
            Text(
                "χ² = ${"%.1f".format(position.chiSquare)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "p = ${formatP(position.pValue)}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (position.significant) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "SESGO",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Text(
            "más visto: ${position.topDigit} (${"%+.1f".format(position.topZ)}σ) · " +
                "menos visto: ${position.lowDigit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatP(value: Double): String =
    if (value < 0.0001) "<0.0001" else "%.4f".format(value)
