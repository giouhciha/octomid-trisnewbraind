package com.octomid.trisbraind.data.remote

import com.octomid.trisbraind.data.local.DrawEntity
import java.time.LocalDate

/**
 * Parser del CSV de la Lotería Nacional.
 *
 * Formato:
 * NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,FECHA,Multiplicador
 *
 * Notas:
 *  - Antes del 03/09/2007 el sorteo era de 4 dígitos (R5 vacío): se descarta para el análisis de 5 urnas.
 *  - `turno` se asigna por orden ascendente de CONCURSO dentro del mismo día
 *    (1=Medio 13h ... 5=Clásico 21h). En 2018+ todos los días tienen exactamente 5 concursos.
 */
object TrisCsvParser {

    data class Parsed(
        val draws: List<DrawEntity>,
        val totalRows: Int,
        val fiveDigitRows: Int
    )

    private data class Row(
        val concurso: Int,
        val fecha: Long,
        val d1: Int,
        val d2: Int,
        val d3: Int,
        val d4: Int,
        val d5: Int,
        val multiplicador: Boolean
    )

    fun parse(csv: String): Parsed {
        val lines = csv.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.isEmpty()) return Parsed(emptyList(), 0, 0)

        val first = lines.first().removePrefix("\uFEFF")
        val start = if (first.contains("CONCURSO", ignoreCase = true)) 1 else 0

        val byFecha = LinkedHashMap<Long, MutableList<Row>>()
        var total = 0
        var fiveDigit = 0

        for (i in start until lines.size) {
            val cols = lines[i].split(',')
            if (cols.size < 8) continue
            total++

            val r5 = cols[6].trim()
            if (r5.isEmpty()) continue // sorteo de 4 dígitos (pre-2007)

            val concurso = cols[1].trim().toIntOrNull() ?: continue
            val fecha = parseFecha(cols[7].trim()) ?: continue
            val d1 = cols[2].trim().toIntOrNull() ?: continue
            val d2 = cols[3].trim().toIntOrNull() ?: continue
            val d3 = cols[4].trim().toIntOrNull() ?: continue
            val d4 = cols[5].trim().toIntOrNull() ?: continue
            val d5 = r5.toIntOrNull() ?: continue
            val multiplicador = cols.getOrNull(8)?.trim().equals("SI", ignoreCase = true)

            fiveDigit++
            byFecha.getOrPut(fecha) { mutableListOf() }
                .add(Row(concurso, fecha, d1, d2, d3, d4, d5, multiplicador))
        }

        val draws = ArrayList<DrawEntity>(fiveDigit)
        for ((_, rows) in byFecha) {
            rows.sortBy { it.concurso }
            rows.forEachIndexed { index, row ->
                draws.add(
                    DrawEntity(
                        concurso = row.concurso,
                        fecha = row.fecha,
                        turno = index + 1,
                        d1 = row.d1,
                        d2 = row.d2,
                        d3 = row.d3,
                        d4 = row.d4,
                        d5 = row.d5,
                        multiplicador = row.multiplicador
                    )
                )
            }
        }
        draws.sortBy { it.concurso }
        return Parsed(draws, total, fiveDigit)
    }

    /** dd/MM/yyyy -> epochDay. */
    private fun parseFecha(value: String): Long? {
        val parts = value.split('/')
        if (parts.size != 3) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        return try {
            LocalDate.of(year, month, day).toEpochDay()
        } catch (e: Exception) {
            null
        }
    }
}
