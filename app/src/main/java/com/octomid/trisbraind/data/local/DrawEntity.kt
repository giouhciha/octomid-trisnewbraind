package com.octomid.trisbraind.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Un concurso (sorteo) de Tris. El numero nunca se modela como categoria de 100,000:
 * siempre son 5 digitos independientes (d1..d5), uno por urna.
 *
 * @param fecha epochDay (dias desde 1970-01-01) para comparaciones eficientes.
 * @param turno 1=Medio 13h, 2=Tres 15h, 3=Extra 17h, 4=Siete 19h, 5=Clasico 21h.
 */
@Entity(
    tableName = "draws",
    indices = [
        Index(value = ["fecha", "turno"], unique = true),
        Index("fecha"),
        Index("turno")
    ]
)
data class DrawEntity(
    @PrimaryKey val concurso: Int,
    val fecha: Long,
    val turno: Int,
    val d1: Int,
    val d2: Int,
    val d3: Int,
    val d4: Int,
    val d5: Int,
    val multiplicador: Boolean
) {
    fun digits(): IntArray = intArrayOf(d1, d2, d3, d4, d5)

    fun asString(): String = "$d1$d2$d3$d4$d5"

    fun digitAt(position: Int): Int = when (position) {
        0 -> d1
        1 -> d2
        2 -> d3
        3 -> d4
        else -> d5
    }
}
