package com.octomid.trisbraind.domain.suggest

import com.octomid.trisbraind.data.local.DrawEntity
import com.octomid.trisbraind.domain.Turno
import com.octomid.trisbraind.domain.stats.FrequencyAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SuggestionEngineTest {

    private fun draw(concurso: Int, turno: Int, digits: IntArray) = DrawEntity(
        concurso = concurso,
        fecha = concurso.toLong(),
        turno = turno,
        d1 = digits[0],
        d2 = digits[1],
        d3 = digits[2],
        d4 = digits[3],
        d5 = digits[4],
        multiplicador = false
    )

    @Test
    fun generaCombinacionesUnicasPorTurnoYSinRepetirEntreTurnos() {
        val rnd = Random(42)
        val byTurno = Turno.todos.associateWith { turno ->
            (0 until 300).map { i -> draw(i, turno.id, IntArray(5) { rnd.nextInt(10) }) }
        }
        val engine = SuggestionEngine(FrequencyAnalyzer())

        val result = engine.suggestAll(byTurno, combosPerTurno = 5, strategy = Strategy.BALANCED)

        assertEquals(5, result.size)
        val all = mutableListOf<String>()
        for (turnoSuggestion in result) {
            assertEquals(5, turnoSuggestion.combos.size)
            assertEquals(5, turnoSuggestion.combos.toSet().size)
            all += turnoSuggestion.combos
        }
        assertEquals(25, all.toSet().size)
    }

    @Test
    fun todasLasEstrategiasGeneranCincoDigitos() {
        val engine = SuggestionEngine(FrequencyAnalyzer())
        val byTurno = Turno.todos.associateWith { turno ->
            List(300) { i -> draw(i, turno.id, IntArray(5) { i % 10 }) }
        }
        for (strategy in Strategy.entries) {
            val result = engine.suggestAll(byTurno, combosPerTurno = 7, strategy = strategy)
            result.forEach { turnoSuggestion ->
                assertEquals(7, turnoSuggestion.combos.size)
                turnoSuggestion.combos.forEach { combo ->
                    assertEquals(5, combo.length)
                    assertTrue(combo.all { it.isDigit() })
                }
            }
        }
    }
}
