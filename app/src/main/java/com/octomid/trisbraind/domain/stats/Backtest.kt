package com.octomid.trisbraind.domain.stats

import com.octomid.trisbraind.data.local.DrawEntity
import kotlin.math.ln

/**
 * Validación walk-forward honesta: mide la estrategia de "dígito más frecuente"
 * contra la línea base uniforme. Si no la supera, no hay ventaja que explotar.
 *
 *  - uniforme: top-1 = 10.0%, log-loss por dígito = ln(10) ≈ 2.302585
 */
data class BacktestResult(
    val samples: Int,
    val top1Accuracy: Double,
    val logLossPerDigit: Double
) {
    val uniformAccuracy: Double get() = 0.1
    val uniformLogLoss: Double get() = ln(10.0)
    val beatsChance: Boolean get() = top1Accuracy > uniformAccuracy && logLossPerDigit < uniformLogLoss
}

object Backtest {

    private const val WARMUP = 500

    fun runFrequencyBaseline(draws: List<DrawEntity>): BacktestResult {
        if (draws.size <= WARMUP) {
            return BacktestResult(0, 0.0, 0.0)
        }
        val counts = Array(5) { IntArray(10) }
        var hits = 0
        var samples = 0
        var logLoss = 0.0

        for ((index, draw) in draws.withIndex()) {
            if (index >= WARMUP) {
                for (p in 0..4) {
                    val c = counts[p]
                    val total = c.sum().toDouble()
                    var best = 0
                    for (d in 1..9) if (c[d] > c[best]) best = d
                    val actual = draw.digitAt(p)
                    if (best == actual) hits++
                    val prob = (c[actual] + 0.5) / (total + 5.0)
                    logLoss += -ln(prob)
                    samples++
                }
            }
            counts[0][draw.d1]++
            counts[1][draw.d2]++
            counts[2][draw.d3]++
            counts[3][draw.d4]++
            counts[4][draw.d5]++
        }
        return BacktestResult(
            samples = samples,
            top1Accuracy = if (samples == 0) 0.0 else hits.toDouble() / samples,
            logLossPerDigit = if (samples == 0) 0.0 else logLoss / samples
        )
    }
}
