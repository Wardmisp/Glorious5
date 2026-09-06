package com.g5.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

// Le fichier nba_top300.db doit être placé dans : shared/src/androidMain/assets/databases/nba_top300.db
private const val DB_NAME = "nba_top300.db"
private const val DB_ASSET_PATH = "databases/nba_top300.db"

/**
 * Room refuse `createFromAsset`/`createFromFile` dès qu'un `SQLiteDriver` explicite est
 * configuré (voir `buildAppDatabase` dans AppDatabase.kt, commun aux deux plateformes) —
 * "Pre-Package Database is not supported when an SQLiteDriver is configured". On reproduit donc
 * la même copie à la main qu'iOS (AppDatabase.ios.kt) plutôt que de laisser Room le faire.
 */
fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(DB_NAME)
    if (!dbFile.exists()) {
        dbFile.parentFile?.mkdirs()
        appContext.assets.open(DB_ASSET_PATH).use { input ->
            dbFile.outputStream().use { output -> input.copyTo(output) }
        }
    }
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    ).fallbackToDestructiveMigration(dropAllTables = true)
}
