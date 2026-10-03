package com.octomid.trisbraind.domain.stats

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/**
 * Prueba de uniformidad (chi-cuadrado) para una urna de 10 dígitos.
 * df = 9. Valor crítico al 5% = 16.919, al 1% = 21.666.
 *
 * Incluye el p-valor exacto con la gamma incompleta regularizada, para no depender
 * de librerías pesadas (Apache Commons, etc.).
 */
object UniformityTest {

    const val DF = 9
    const val CRITICAL_05 = 16.919
    const val CRITICAL_01 = 21.666

    fun chiSquare(counts: IntArray): Double {
        val n = counts.sum().toDouble()
        if (n <= 0.0) return 0.0
        val expected = n / counts.size
        var sum = 0.0
        for (o in counts) {
            val diff = o - expected
            sum += diff * diff / expected
        }
        return sum
    }

    fun pValue(chiSquare: Double): Double = regularizedGammaQ(DF / 2.0, chiSquare / 2.0)

    /** ¿Se aleja de lo esperado al 5%? */
    fun isDeviant(counts: IntArray): Boolean = chiSquare(counts) > CRITICAL_05

    // --- Gamma incompleta regularizada (Numerical Recipes, sin dependencias) ---

    fun regularizedGammaQ(a: Double, x: Double): Double {
        if (x < 0 || a <= 0) return 1.0
        if (x == 0.0) return 1.0
        return if (x < a + 1.0) 1.0 - gammaSeries(a, x) else gammaContinuedFraction(a, x)
    }

    private fun gammaSeries(a: Double, x: Double): Double {
        val itMax = 200
        val eps = 3e-7
        var ap = a
        var sum = 1.0 / a
        var del = sum
        for (i in 1..itMax) {
            ap += 1.0
            del *= x / ap
            sum += del
            if (abs(del) < abs(sum) * eps) break
        }
        return sum * exp(-x + a * ln(x) - lnGamma(a))
    }

    private fun gammaContinuedFraction(a: Double, x: Double): Double {
        val itMax = 200
        val eps = 3e-7
        val fpMin = 1e-30
        var b = x + 1.0 - a
        var c = 1.0 / fpMin
        var d = 1.0 / b
        var h = d
        for (i in 1..itMax) {
            val an = -i * (i - a).toDouble()
            b += 2.0
            d = an * d + b
            if (abs(d) < fpMin) d = fpMin
            c = b + an / c
            if (abs(c) < fpMin) c = fpMin
            d = 1.0 / d
            val del = d * c
            h *= del
            if (abs(del - 1.0) < eps) break
        }
        return exp(-x + a * ln(x) - lnGamma(a)) * h
    }

    private fun lnGamma(xx: Double): Double {
        val cof = doubleArrayOf(
            76.18009172947146, -86.50532032941677, 24.01409824083091,
            -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5
        )
        var x = xx
        var y = xx
        var tmp = x + 5.5
        tmp -= (x + 0.5) * ln(tmp)
        var ser = 1.000000000190015
        for (j in 0..5) {
            y += 1.0
            ser += cof[j] / y
        }
        return -tmp + ln(2.5066282746310005 * ser / x)
    }
}
