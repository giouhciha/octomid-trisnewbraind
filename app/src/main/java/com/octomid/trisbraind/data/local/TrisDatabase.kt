package com.octomid.trisbraind.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DrawEntity::class, PredictionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class TrisDatabase : RoomDatabase() {

    abstract fun drawDao(): DrawDao
    abstract fun predictionDao(): PredictionDao

    companion object {
        /** v1 -> v2: agregó (temporalmente) una tabla de boletos. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fecha` INTEGER NOT NULL,
                        `turno` INTEGER NOT NULL,
                        `combo` TEXT NOT NULL,
                        `monto` REAL NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bets_fecha_turno` ON `bets` (`fecha`, `turno`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_bets_fecha` ON `bets` (`fecha`)")
            }
        }

        /** v2 -> v3: se eliminó la función de boletos. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `bets`")
            }
        }

        /** v3 -> v4: se agregó el histórico de pronósticos. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `predictions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `fecha` INTEGER NOT NULL,
                        `turno` INTEGER NOT NULL,
                        `strategy` TEXT NOT NULL,
                        `combos` TEXT NOT NULL,
                        `sampleSize` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_predictions_fecha_turno` " +
                        "ON `predictions` (`fecha`, `turno`)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_predictions_fecha` ON `predictions` (`fecha`)")
            }
        }
    }
}
