package com.octomid.trisbraind.domain.stats

import com.octomid.trisbraind.data.local.DrawEntity
import com.octomid.trisbraind.domain.Turno
import kotlin.math.sqrt

/**
 * Monitor de sesgo con ventana rodante.
 *
 * Para cada turno y cada posición (urna) se corre un chi-cuadrado sobre los
 * últimos `window` sorteos y se calcula el p-valor. Para evitar falsos positivos
 * por comparar muchas urnas a la vez (5 turnos × 5 posiciones = 25 pruebas), se
 * aplica la corrección de Bonferroni: solo se marca una urna como sesgada si su
 * p-valor cae por debajo de `alpha / totalPruebas`.
 *
 * Con esa corrección, el número esperado de falsos positivos vuelve a ser ~alpha.
 */
object BiasMonitor {

    data class PositionResult(
        val position: Int,
        val chiSquare: Double,
        val pValue: Double,
        val significant: Boolean,
        val topDigit: Int,
        val topZ: Double,
        val lowDigit: Int
    )

    data class TurnoResult(
        val turno: Turno,
        val sampleSize: Int,
        val positions: List<PositionResult>
    )

    data class Report(
        val window: Int,
        val totalTests: Int,
        val alpha: Double,
        val correctedAlpha: Double,
        val expectedFalsePositives: Double,
        val significantCount: Int,
        val turnos: List<TurnoResult>
    ) {
        val anySignificant: Boolean get() = significantCount > 0
    }

    val windows: List<Int> = listOf(100, 300, 500, 0) // 0 = todo el histórico

    fun analyze(
        drawsByTurno: Map<Turno, List<DrawEntity>>,
        window: Int,
        alpha: Double = 0.05
    ): Report {
        val testsPerTurno = 5
        val totalTests = Turno.todos.size * testsPerTurno
        val correctedAlpha = alpha / totalTests

        val turnoResults = Turno.todos.map { turno ->
            val all = drawsByTurno[turno].orEmpty()
            val sample = if (window > 0 && all.size > window) all.takeLast(window) else all
            val counts = Array(5) { IntArray(10) }
            for (draw in sample) {
                counts[0][draw.d1]++
                counts[1][draw.d2]++
                counts[2][draw.d3]++
                counts[3][draw.d4]++
                counts[4][draw.d5]++
            }
            val positions = (0 until 5).map { p ->
                val c = counts[p]
                val chi = UniformityTest.chiSquare(c)
                val pValue = UniformityTest.pValue(chi)
                val n = c.sum()
                val expected = n / 10.0
                val sd = sqrt(n * 0.1 * 0.9)
                val top = c.indices.maxByOrNull { c[it] } ?: 0
                val low = c.indices.minByOrNull { c[it] } ?: 0
                PositionResult(
                    position = p + 1,
                    chiSquare = chi,
                    pValue = pValue,
                    significant = pValue < correctedAlpha,
                    topDigit = top,
                    topZ = if (sd > 0.0) (c[top] - expected) / sd else 0.0,
                    lowDigit = low
                )
            }
            TurnoResult(turno = turno, sampleSize = sample.size, positions = positions)
        }

        val significantCount = turnoResults.sumOf { result ->
            result.positions.count { it.significant }
        }

        return Report(
            window = window,
            totalTests = totalTests,
            alpha = alpha,
            correctedAlpha = correctedAlpha,
            expectedFalsePositives = totalTests * correctedAlpha,
            significantCount = significantCount,
            turnos = turnoResults
        )
    }
}
