package com.octomid.trisbraind.data.remote

import com.octomid.trisbraind.domain.Turno
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrisCsvParserTest {

    private val csv = """
        NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,FECHA,Multiplicador
        60,1,1,1,1,1,1,01/01/2018,NO
        60,3,0,1,2,3,4,02/01/2018,NO
        60,2,5,6,7,8,9,02/01/2018,SI
        60,0,9,9,9,9,,31/12/2017,NO
    """.trimIndent()

    @Test
    fun descartaSorteosDeCuatroDigitos() {
        val parsed = TrisCsvParser.parse(csv)
        assertEquals(3, parsed.draws.size)
        assertEquals(4, parsed.totalRows)
        assertEquals(3, parsed.fiveDigitRows)
    }

    @Test
    fun asignaTurnoPorOrdenAscendenteDeConcurso() {
        val parsed = TrisCsvParser.parse(csv)
        val dia2 = parsed.draws
            .filter { it.concurso == 2 || it.concurso == 3 }
            .sortedBy { it.concurso }

        assertEquals(Turno.MEDIO.id, dia2[0].turno) // concurso 2 -> primero del día
        assertEquals(Turno.TRES.id, dia2[1].turno)  // concurso 3 -> segundo del día
        assertEquals("56789", dia2[0].asString())
        assertTrue(dia2[0].multiplicador)
    }
}
