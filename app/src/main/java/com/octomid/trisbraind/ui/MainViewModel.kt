package com.octomid.trisbraind.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.octomid.trisbraind.TrisApp
import com.octomid.trisbraind.data.local.PredictionEntity
import com.octomid.trisbraind.data.local.PredictionWithDraw
import com.octomid.trisbraind.domain.Turno
import com.octomid.trisbraind.domain.stats.Backtest
import com.octomid.trisbraind.domain.stats.BacktestResult
import com.octomid.trisbraind.domain.stats.BiasMonitor
import com.octomid.trisbraind.domain.suggest.Strategy
import com.octomid.trisbraind.domain.suggest.TurnoSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TurnoView(
    val suggestion: TurnoSuggestion,
    val backtest: BacktestResult
)

data class MainUiState(
    val syncing: Boolean = false,
    val generating: Boolean = false,
    val totalDraws: Int = 0,
    val lastFecha: String? = null,
    val message: String? = null,
    val isError: Boolean = false,
    val combosPerTurno: Int = 5,
    val strategy: Strategy = Strategy.BALANCED,
    val turnos: List<TurnoView> = emptyList(),
    val history: List<PredictionWithDraw> = emptyList(),
    val historyCount: Int = 0,
    val showHistory: Boolean = false,
    val showBias: Boolean = false,
    val biasWindow: Int = 300,
    val biasReport: BiasMonitor.Report? = null,
    val biasLoading: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as TrisApp).container
    private val repository = container.repository
    private val predictionRepository = container.predictionRepository
    private val engine = container.suggestionEngine

    /** Alcance del análisis: 2018 en adelante. */
    private val scopeStartEpochDay = LocalDate.of(2018, 1, 1).toEpochDay()

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    init {
        refreshStats()
        refreshHistory()
    }

    fun sync() {
        if (_state.value.syncing) return
        viewModelScope.launch {
            _state.update { it.copy(syncing = true, message = "Descargando historial de Tris…", isError = false) }
            runCatching { repository.syncFromNetwork() }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            syncing = false,
                            totalDraws = result.total,
                            lastFecha = formatDate(result.maxFecha),
                            message = "Listo: ${result.inserted} concurso(s) nuevo(s). Total en base: ${result.total}.",
                            isError = false
                        )
                    }
                    // Al llegar resultados nuevos, se actualiza el acierto de los pronósticos guardados.
                    refreshHistory()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            syncing = false,
                            message = "No se pudo actualizar: ${error.message}",
                            isError = true
                        )
                    }
                }
        }
    }

    fun suggest() {
        if (_state.value.generating) return
        viewModelScope.launch {
            _state.update { it.copy(generating = true, message = "Analizando cada sorteo por separado…", isError = false) }
            runCatching {
                val current = _state.value
                val drawsByTurno = Turno.todos.associateWith { turno ->
                    repository.drawsByTurnoSince(turno.id, scopeStartEpochDay)
                }
                val suggestions = engine.suggestAll(
                    drawsByTurno = drawsByTurno,
                    combosPerTurno = current.combosPerTurno,
                    strategy = current.strategy
                )
                val views = suggestions.map { suggestion ->
                    TurnoView(
                        suggestion = suggestion,
                        backtest = Backtest.runFrequencyBaseline(drawsByTurno[suggestion.turno].orEmpty())
                    )
                }
                // Guarda los pronósticos del día, uno por sorteo.
                val hoy = LocalDate.now().toEpochDay()
                val now = System.currentTimeMillis()
                val toSave = suggestions.map { suggestion ->
                    PredictionEntity(
                        fecha = hoy,
                        turno = suggestion.turno.id,
                        strategy = suggestion.strategy.name,
                        combos = suggestion.combos.joinToString(","),
                        sampleSize = suggestion.sampleSize,
                        createdAt = now
                    )
                }
                predictionRepository.saveAll(toSave)
                views
            }.onSuccess { views ->
                _state.update {
                    it.copy(
                        generating = false,
                        turnos = views,
                        message = "Sugerencias generadas y guardadas para hoy.",
                        isError = false
                    )
                }
                refreshHistory()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        generating = false,
                        message = "No se pudieron generar: ${error.message}",
                        isError = true
                    )
                }
            }
        }
    }

    fun setCombosPerTurno(value: Int) {
        _state.update { it.copy(combosPerTurno = value.coerceIn(1, 20)) }
    }

    fun setStrategy(strategy: Strategy) {
        _state.update { it.copy(strategy = strategy) }
    }

    fun showHistory() {
        refreshHistory()
        _state.update { it.copy(showHistory = true) }
    }

    fun hideHistory() {
        _state.update { it.copy(showHistory = false) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            runCatching { predictionRepository.clear() }.onSuccess { refreshHistory() }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun showBias() {
        _state.update { it.copy(showBias = true) }
        computeBias()
    }

    fun hideBias() {
        _state.update { it.copy(showBias = false) }
    }

    fun setBiasWindow(window: Int) {
        _state.update { it.copy(biasWindow = window) }
        computeBias()
    }

    private fun computeBias() {
        viewModelScope.launch {
            _state.update { it.copy(biasLoading = true) }
            runCatching {
                val window = _state.value.biasWindow
                val drawsByTurno = Turno.todos.associateWith { turno ->
                    repository.drawsByTurnoSince(turno.id, scopeStartEpochDay)
                }
                BiasMonitor.analyze(drawsByTurno, window)
            }.onSuccess { report ->
                _state.update { it.copy(biasLoading = false, biasReport = report) }
            }.onFailure {
                _state.update { it.copy(biasLoading = false, biasReport = null) }
            }
        }
    }

    private fun refreshHistory() {
        viewModelScope.launch {
            runCatching { predictionRepository.history() }.onSuccess { history ->
                _state.update { it.copy(history = history, historyCount = history.size) }
            }
        }
    }

    private fun refreshStats() {
        viewModelScope.launch {
            runCatching { repository.stats() }.onSuccess { stats ->
                _state.update {
                    it.copy(totalDraws = stats.total, lastFecha = formatDate(stats.maxFecha))
                }
            }
        }
    }

    private fun formatDate(epochDay: Long?): String? =
        epochDay?.let { LocalDate.ofEpochDay(it).toString() }
}
