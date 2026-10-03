package com.octomid.trisbraind.domain.stats

import com.octomid.trisbraind.data.local.DrawEntity
import com.octomid.trisbraind.domain.Turno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BiasMonitorTest {

    private fun balanced(turno: Turno, n: Int, start: Int): List<DrawEntity> =
        (0 until n).map { i ->
            val digit = (i / (n / 10)) % 10
            DrawEntity(start + i, i.toLong(), turno.id, digit, digit, digit, digit, digit, false)
        }

    private fun biasedFirstPosition(turno: Turno, n: Int, start: Int): List<DrawEntity> =
        (0 until n).map { i ->
            val first = if (i < 450) 0 else 1 + ((i - 450) / 6).coerceAtMost(8)
            val other = (i / (n / 10)) % 10
            DrawEntity(start + i, i.toLong(), turno.id, first, other, other, other, other, false)
        }

    @Test
    fun noMarcaSesgoConUrnasUniformes() {
        val draws = mapOf(
            Turno.MEDIO to balanced(Turno.MEDIO, 500, 0),
            Turno.TRES to balanced(Turno.TRES, 500, 0),
            Turno.EXTRA to balanced(Turno.EXTRA, 500, 0),
            Turno.SIETE to balanced(Turno.SIETE, 500, 0),
            Turno.CLASICO to balanced(Turno.CLASICO, 500, 0)
        )

        val report = BiasMonitor.analyze(draws, window = 0)
        assertEquals(0, report.significantCount)
    }

    @Test
    fun detectaSesgoFuerteYCorrigeMultiplesPruebas() {
        val draws = mapOf(
            Turno.MEDIO to biasedFirstPosition(Turno.MEDIO, 500, 0),
            Turno.TRES to balanced(Turno.TRES, 500, 0),
            Turno.EXTRA to balanced(Turno.EXTRA, 500, 0),
            Turno.SIETE to balanced(Turno.SIETE, 500, 0),
            Turno.CLASICO to balanced(Turno.CLASICO, 500, 0)
        )

        val report = BiasMonitor.analyze(draws, window = 0)

        assertTrue(report.anySignificant)
        assertEquals(25, report.totalTests)
        assertTrue(report.correctedAlpha < 0.05)

        val medio = report.turnos.first { it.turno == Turno.MEDIO }
        val r1 = medio.positions.first { it.position == 1 }
        assertTrue("R1 debería estar sesgada", r1.significant)
        assertEquals(0, r1.topDigit)
    }
}
