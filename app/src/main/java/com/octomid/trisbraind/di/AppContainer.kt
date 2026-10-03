package com.octomid.trisbraind.di

import android.content.Context
import androidx.room.Room
import com.octomid.trisbraind.data.local.TrisDatabase
import com.octomid.trisbraind.data.remote.TrisCsvDataSource
import com.octomid.trisbraind.data.repo.PredictionRepository
import com.octomid.trisbraind.data.repo.TrisRepository
import com.octomid.trisbraind.domain.stats.FrequencyAnalyzer
import com.octomid.trisbraind.domain.suggest.SuggestionEngine

/**
 * Contenedor de dependencias manual (sin Hilt): menos piezas, mas facil de mantener.
 */
class AppContainer(context: Context) {

    private val database: TrisDatabase = Room.databaseBuilder(
        context.applicationContext,
        TrisDatabase::class.java,
        "tris.db"
    )
        .addMigrations(
            TrisDatabase.MIGRATION_1_2,
            TrisDatabase.MIGRATION_2_3,
            TrisDatabase.MIGRATION_3_4
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    val repository: TrisRepository = TrisRepository(
        source = TrisCsvDataSource(),
        dao = database.drawDao()
    )

    val predictionRepository: PredictionRepository = PredictionRepository(database.predictionDao())

    val frequencyAnalyzer: FrequencyAnalyzer = FrequencyAnalyzer()

    val suggestionEngine: SuggestionEngine = SuggestionEngine(frequencyAnalyzer)
}
