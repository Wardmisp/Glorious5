package com.g5.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val DB_NAME = "nba_top300.db"

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(name = prepackagedDatabasePath())

/**
 * Room KMP ne sait pas encore pré-remplir une base depuis un fichier embarqué en dehors
 * d'Android (`createFromAsset`/`createFromFile` : Android uniquement) — on reproduit donc la
 * même chose à la main : copier `nba_top300.db` (embarqué dans le bundle iOS via
 * `iosApp/project.yml`) vers un répertoire inscriptible au premier lancement, puis pointer Room
 * dessus. `Application Support` plutôt que `Documents` pour ne pas inclure ce fichier interne
 * dans les sauvegardes iCloud de l'utilisateur.
 */
@OptIn(ExperimentalForeignApi::class)
private fun prepackagedDatabasePath(): String {
    val fileManager = NSFileManager.defaultManager
    val appSupportDir = fileManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    ) ?: error("Impossible de localiser le répertoire Application Support")

    val destinationUrl = appSupportDir.URLByAppendingPathComponent(DB_NAME)
        ?: error("Impossible de construire le chemin de destination pour $DB_NAME")
    val destinationPath = requireNotNull(destinationUrl.path) { "Chemin de destination invalide pour $DB_NAME" }

    if (!fileManager.fileExistsAtPath(destinationPath)) {
        val bundlePath = NSBundle.mainBundle.pathForResource("nba_top300", ofType = "db")
            ?: error("$DB_NAME introuvable dans le bundle de l'app iOS (voir iosApp/project.yml)")
        fileManager.copyItemAtPath(bundlePath, toPath = destinationPath, error = null)
    }

    return destinationPath
}
