package com.octomid.trisbraind.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PredictionWithDrawTest {

    private fun prediction(combos: String, winning: String?): PredictionWithDraw {
        val digits: List<Int>? = winning?.map { it.digitToInt() }
        return PredictionWithDraw(
            prediction = PredictionEntity(
                id = 0,
                fecha = 1L,
                turno = 1,
                strategy = "BALANCED",
                combos = combos,
                sampleSize = 100,
                createdAt = 0L
            ),
            d1 = digits?.get(0),
            d2 = digits?.get(1),
            d3 = digits?.get(2),
            d4 = digits?.get(3),
            d5 = digits?.get(4)
        )
    }

    @Test
    fun parseaCombosSeparadosPorComa() {
        assertEquals(listOf("12345", "67890"), prediction("12345,67890", null).combos)
    }

    @Test
    fun detectaAciertoEntreVariasCombinaciones() {
        val conAcierto = prediction("11111,22222", "22222")
        assertEquals("22222", conAcierto.hitCombo)

        val sinAcierto = prediction("11111,33333", "22222")
        assertNull(sinAcierto.hitCombo)
    }

    @Test
    fun pendienteCuandoNoHaySorteo() {
        assertNull(prediction("11111", null).winningCombo)
        assertNull(prediction("11111", null).hitCombo)
    }
}
