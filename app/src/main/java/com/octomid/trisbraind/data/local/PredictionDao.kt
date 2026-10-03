package com.octomid.trisbraind.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Pronóstico enriquecido con el resultado real del sorteo (si ya existe).
 * Permite saber si alguna combinación guardada coincidió.
 */
data class PredictionWithDraw(
    @Embedded val prediction: PredictionEntity,
    val d1: Int?,
    val d2: Int?,
    val d3: Int?,
    val d4: Int?,
    val d5: Int?
) {
    val combos: List<String>
        get() = prediction.combos.split(',').map { it.trim() }.filter { it.isNotEmpty() }

    val winningCombo: String?
        get() = if (d1 != null && d2 != null && d3 != null && d4 != null && d5 != null) {
            "$d1$d2$d3$d4$d5"
        } else {
            null
        }

    val hitCombo: String?
        get() = winningCombo?.let { winning -> combos.firstOrNull { it == winning } }
}

@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(predictions: List<PredictionEntity>)

    @Query(
        """
        SELECT p.*, d.d1 AS d1, d.d2 AS d2, d.d3 AS d3, d.d4 AS d4, d.d5 AS d5
        FROM predictions p
        LEFT JOIN draws d ON p.fecha = d.fecha AND p.turno = d.turno
        ORDER BY p.fecha DESC, p.turno ASC
        """
    )
    suspend fun allWithDraws(): List<PredictionWithDraw>

    @Query("SELECT COUNT(*) FROM predictions")
    suspend fun count(): Int

    @Query("DELETE FROM predictions")
    suspend fun clear()
}
