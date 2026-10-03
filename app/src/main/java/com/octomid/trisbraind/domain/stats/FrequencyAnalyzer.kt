package com.octomid.trisbraind.domain.stats

import com.octomid.trisbraind.data.local.DrawEntity

/**
 * Cuenta ocurrencias por posición (5 urnas) de forma O(n), una sola pasada.
 */
class FrequencyAnalyzer {

    /** counts[position][digit] con position 0..4 y digit 0..9. */
    fun counts(draws: List<DrawEntity>): Array<IntArray> {
        val result = Array(5) { IntArray(10) }
        for (draw in draws) {
            result[0][draw.d1]++
            result[1][draw.d2]++
            result[2][draw.d3]++
            result[3][draw.d4]++
            result[4][draw.d5]++
        }
        return result
    }

    fun total(draws: List<DrawEntity>): Int = draws.size
}
