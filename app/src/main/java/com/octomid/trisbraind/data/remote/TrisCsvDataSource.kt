package com.octomid.trisbraind.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Descarga el historico de Tris. El endpoint responde CSV aunque la ruta parezca HTML.
 * No hay backend propio: la app habla directo con la Lotería Nacional.
 */
class TrisCsvDataSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        const val TRIS_URL =
            "https://www.loterianacional.gob.mx/Home/Historicos?ARHP=VAByAGkAcwA="
    }

    fun fetchCsvBlocking(): String {
        val request = Request.Builder()
            .url(TRIS_URL)
            .header("User-Agent", "Mozilla/5.0 (Android) TrisBrain/0.1")
            .header("Accept", "text/csv,text/plain,*/*")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            return response.body?.string() ?: error("Respuesta vacía")
        }
    }
}
