package com.octomid.trisbraind.data.repo

import com.octomid.trisbraind.data.local.PredictionDao
import com.octomid.trisbraind.data.local.PredictionEntity
import com.octomid.trisbraind.data.local.PredictionWithDraw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredictionRepository(private val dao: PredictionDao) {

    suspend fun saveAll(predictions: List<PredictionEntity>) = withContext(Dispatchers.IO) {
        dao.upsertAll(predictions)
    }

    suspend fun history(): List<PredictionWithDraw> = withContext(Dispatchers.IO) {
        dao.allWithDraws()
    }

    suspend fun count(): Int = withContext(Dispatchers.IO) { dao.count() }

    suspend fun clear() = withContext(Dispatchers.IO) { dao.clear() }
}
