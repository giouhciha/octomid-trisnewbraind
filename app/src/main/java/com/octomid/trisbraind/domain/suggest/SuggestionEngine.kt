package com.octomid.trisbraind.domain.suggest

import com.octomid.trisbraind.data.local.DrawEntity
import com.octomid.trisbraind.domain.Turno
import com.octomid.trisbraind.domain.stats.FrequencyAnalyzer
import com.octomid.trisbraind.domain.stats.UniformityTest
import kotlin.random.Random

enum class Strategy(val label: String, val detalle: String) {
    BALANCED(
        "Cobertura balanceada",
        "Reparte cada dígito de forma pareja en cada urna. Es la única defendible si el sorteo es justo."
    ),
    FREQUENCY(
        "Frecuencia suavizada",
        "Usa los dígitos más vistos por posición con suavizado Laplace. Sin evidencia de ventaja."
    ),
    MIXED(
        "Mixta",
        "Mitad cobertura, mitad frecuencia."
    )
}

data class TurnoSuggestion(
    val turno: Turno,
    val sampleSize: Int,
    val strategy: Strategy,
    val combos: List<String>,
    /** Posiciones (1..5) cuya urna se desvía de lo uniforme al 5%. */
    val deviantPositions: List<Int>
)

/**
 * Genera combinaciones por turno, modelando cada posición como una urna independiente.
 * Nunca elige "el número completo": construye dígito por dígito.
 */
class SuggestionEngine(private val analyzer: FrequencyAnalyzer) {

    fun suggestAll(
        drawsByTurno: Map<Turno, List<DrawEntity>>,
        combosPerTurno: Int,
        strategy: Strategy
    ): List<TurnoSuggestion> {
        val used = HashSet<String>()
        val out = ArrayList<TurnoSuggestion>(Turno.todos.size)
        for (turno in Turno.todos) {
            val draws = drawsByTurno[turno].orEmpty()
            val counts = analyzer.counts(draws)
            val deviants = (0 until 5)
                .filter { UniformityTest.isDeviant(counts[it]) }
                .map { it + 1 }
            val combos = generate(counts, combosPerTurno, strategy, used)
            used.addAll(combos)
            out.add(TurnoSuggestion(turno, draws.size, strategy, combos, deviants))
        }
        return out
    }

    private fun generate(
        counts: Array<IntArray>,
        n: Int,
        strategy: Strategy,
        avoid: Set<String>
    ): List<String> = when (strategy) {
        Strategy.BALANCED -> balanced(n, avoid)
        Strategy.FREQUENCY -> frequency(counts, n, avoid)
        Strategy.MIXED -> {
            val half = n / 2
            val first = balanced(half, avoid)
            val second = frequency(counts, n - half, avoid + first)
            first + second
        }
    }

    /**
     * Cobertura balanceada: para cada urna se reparten los 10 dígitos en cuotas iguales,
     * con bolsas barajadas distintas por posición. Cada dígito sale ~n/10 veces.
     */
    private fun balanced(n: Int, avoid: Set<String>): List<String> {
        if (n <= 0) return emptyList()
        val sequences = Array(5) { IntArray(n) }
        for (p in 0..4) {
            var i = 0
            while (i < n) {
                val bag = (0..9).shuffled()
                for (digit in bag) {
                    if (i >= n) break
                    sequences[p][i] = digit
                    i++
                }
            }
        }
        val result = LinkedHashSet<String>()
        for (i in 0 until n) {
            val combo = buildString {
                for (p in 0..4) append(sequences[p][i])
            }
            if (combo !in avoid) result.add(combo)
        }
        fillRandom(result, n, avoid)
        return result.take(n).toList()
    }

    /** Frecuencia suavizada: muestreo ponderado por posición (Laplace +0.5). */
    private fun frequency(counts: Array<IntArray>, n: Int, avoid: Set<String>): List<String> {
        if (n <= 0) return emptyList()
        val weights = Array(5) { p -> DoubleArray(10) { d -> counts[p][d] + 0.5 } }
        val result = LinkedHashSet<String>()
        var guard = 0
        while (result.size < n && guard < n * 500) {
            guard++
            val combo = buildString {
                for (p in 0..4) append(sample(weights[p]))
            }
            if (combo !in avoid) result.add(combo)
        }
        fillRandom(result, n, avoid)
        return result.take(n).toList()
    }

    private fun fillRandom(result: MutableSet<String>, n: Int, avoid: Set<String>) {
        var guard = 0
        while (result.size < n && guard < n * 500) {
            guard++
            val combo = randomCombo()
            if (combo !in avoid && combo !in result) result.add(combo)
        }
    }

    private fun sample(weights: DoubleArray): Int {
        val total = weights.sum()
        var r = Random.nextDouble() * total
        for (d in weights.indices) {
            r -= weights[d]
            if (r <= 0.0) return d
        }
        return weights.lastIndex
    }

    private fun randomCombo(): String = buildString {
        repeat(5) { append(Random.nextInt(10)) }
    }
}
