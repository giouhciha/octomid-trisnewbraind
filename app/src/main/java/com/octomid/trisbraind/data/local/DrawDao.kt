package com.octomid.trisbraind.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DrawDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(draws: List<DrawEntity>)

    @Query("SELECT MAX(concurso) FROM draws")
    suspend fun maxConcurso(): Int?

    @Query("SELECT COUNT(*) FROM draws")
    suspend fun count(): Int

    @Query("SELECT MAX(fecha) FROM draws")
    suspend fun maxFecha(): Long?

    @Query("SELECT * FROM draws WHERE fecha >= :fromEpochDay ORDER BY concurso ASC")
    suspend fun getSince(fromEpochDay: Long): List<DrawEntity>

    @Query("SELECT * FROM draws WHERE turno = :turno AND fecha >= :fromEpochDay ORDER BY concurso ASC")
    suspend fun getByTurnoSince(turno: Int, fromEpochDay: Long): List<DrawEntity>
}
