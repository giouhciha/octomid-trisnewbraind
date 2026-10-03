package com.octomid.trisbraind.data.repo

import com.octomid.trisbraind.data.local.DrawDao
import com.octomid.trisbraind.data.local.DrawEntity
import com.octomid.trisbraind.data.remote.TrisCsvDataSource
import com.octomid.trisbraind.data.remote.TrisCsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrisRepository(
    private val source: TrisCsvDataSource,
    private val dao: DrawDao
) {

    data class SyncResult(
        val inserted: Int,
        val total: Int,
        val maxFecha: Long?
    )

    data class DbStats(
        val total: Int,
        val maxFecha: Long?
    )

    /**
     * Descarga el CSV completo y hace upsert solo de los concursos nuevos.
     * El endpoint siempre devuelve todo el histórico (~1 MB), es barato y evita paginación.
     */
    suspend fun syncFromNetwork(): SyncResult = withContext(Dispatchers.IO) {
        val csv = source.fetchCsvBlocking()
        val parsed = TrisCsvParser.parse(csv)
        val existingMax = dao.maxConcurso()
        val nuevos = if (existingMax == null) {
            parsed.draws
        } else {
            parsed.draws.filter { it.concurso > existingMax }
        }
        if (nuevos.isNotEmpty()) {
            // Inserta en bloques para no cargar un solo statement gigante.
            nuevos.chunked(2000).forEach { dao.upsertAll(it) }
        }
        SyncResult(
            inserted = nuevos.size,
            total = dao.count(),
            maxFecha = dao.maxFecha()
        )
    }

    suspend fun drawsSince(fromEpochDay: Long): List<DrawEntity> = withContext(Dispatchers.IO) {
        dao.getSince(fromEpochDay)
    }

    suspend fun drawsByTurnoSince(turno: Int, fromEpochDay: Long): List<DrawEntity> =
        withContext(Dispatchers.IO) {
            dao.getByTurnoSince(turno, fromEpochDay)
        }

    suspend fun stats(): DbStats = withContext(Dispatchers.IO) {
        DbStats(dao.count(), dao.maxFecha())
    }
}
