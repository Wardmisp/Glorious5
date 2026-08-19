package com.example.androididea.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlayerSeason::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerSeasonDao(): PlayerSeasonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Le fichier nba_top300.db doit être placé dans :
        // app/src/main/assets/databases/nba_top300.db
        private const val DB_NAME = "nba_top300.db"
        private const val DB_ASSET_PATH = "databases/nba_top300.db"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DB_NAME
                    )
                    .createFromAsset(DB_ASSET_PATH)
                    // Room copie automatiquement la BDD des assets vers le
                    // stockage interne au premier lancement. Comme la base
                    // est pré-remplie et en lecture seule dans l'app, on
                    // n'a pas besoin de Migration tant que le schéma ne change pas.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
