package com.g5.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// Le fichier nba_top300.db doit être placé dans : shared/src/androidMain/assets/databases/nba_top300.db
private const val DB_NAME = "nba_top300.db"
private const val DB_ASSET_PATH = "databases/nba_top300.db"

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(DB_NAME).absolutePath
    )
        // Room copie automatiquement la BDD des assets vers le stockage interne au premier
        // lancement. Comme la base est pré-remplie et en lecture seule dans l'app, on n'a pas
        // besoin de Migration tant que le schéma ne change pas.
        .createFromAsset(DB_ASSET_PATH)
        .fallbackToDestructiveMigration(dropAllTables = true)
}
