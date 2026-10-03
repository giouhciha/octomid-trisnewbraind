package com.octomid.trisbraind.domain

/**
 * Los 5 sorteos diarios de Tris, en orden ascendente de horario.
 * Ese mismo orden es el que se usa para clasificar los concursos de un día.
 */
enum class Turno(val id: Int, val nombre: String, val hora: String) {
    MEDIO(1, "Tris Medio día", "13:00"),
    TRES(2, "Tris de las Tres", "15:00"),
    EXTRA(3, "Tris Extra", "17:00"),
    SIETE(4, "Tris de las Siete", "19:00"),
    CLASICO(5, "Tris Clásico", "21:00");

    companion object {
        fun fromId(id: Int): Turno? = entries.firstOrNull { it.id == id }
        val todos: List<Turno> get() = entries.toList()
    }
}
