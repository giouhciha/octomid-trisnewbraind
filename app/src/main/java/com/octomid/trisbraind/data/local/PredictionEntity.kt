package com.octomid.trisbraind.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Pronóstico guardado para un sorteo concreto (fecha + turno).
 * Se conserva el último generado para ese par fecha/turno.
 *
 * @param combos combinaciones separadas por coma, p. ej. "87706,18537,95010".
 * @param createdAt epoch millis del momento en que se generó.
 */
@Entity(
    tableName = "predictions",
    indices = [
        Index(value = ["fecha", "turno"], unique = true),
        Index("fecha")
    ]
)
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: Long,
    val turno: Int,
    val strategy: String,
    val combos: String,
    val sampleSize: Int,
    val createdAt: Long
)
