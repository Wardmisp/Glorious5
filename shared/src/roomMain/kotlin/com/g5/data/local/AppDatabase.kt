package com.g5.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(entities = [PlayerSeason::class], version = 1, exportSchema = false)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerSeasonDao(): PlayerSeasonDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/** Termine la construction commencée par la fonction `getDatabaseBuilder` de chaque plateforme
 * (Android a besoin d'un `Context`, pas iOS, donc ce n'est volontairement pas un simple
 * expect/actual — voir AppDatabase.android.kt / AppDatabase.ios.kt et les DatabaseModule
 * associés). */
fun buildAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
    builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
