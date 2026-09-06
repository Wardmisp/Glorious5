package com.g5.data.local

import com.g5.di.ensureKoinStarted
import kotlinx.coroutines.runBlocking

/** Appelé uniquement depuis `PlatformBridgeTests.swift` pour prouver que la BDD embarquée
 * (copie depuis le bundle + ouverture Room) fonctionne réellement sur le simulateur iOS — pas
 * seulement que ça compile. Passe par Koin (comme l'app réelle) plutôt que de construire une
 * seconde instance à la main : `iosAppTests` tourne dans le même process que l'app, donc un
 * second `RoomDatabase.Builder` pointant sur le même fichier risquerait une ouverture
 * concurrente. */
fun debugFirstPlayerName(): String? = runBlocking {
    ensureKoinStarted().get<AppDatabase>().playerSeasonDao().getAllList().firstOrNull()?.player
}
