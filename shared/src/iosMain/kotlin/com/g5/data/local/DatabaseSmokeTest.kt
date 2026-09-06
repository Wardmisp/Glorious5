package com.g5.data.local

import kotlinx.coroutines.runBlocking

/** Appelé uniquement depuis `PlatformBridgeTests.swift` pour prouver que la BDD embarquée
 * (copie depuis le bundle + ouverture Room) fonctionne réellement sur le simulateur iOS — pas
 * seulement que ça compile. Ne passe pas par Koin (pas encore démarré côté iOS, voir PR
 * suivante) : construit la base directement. */
fun debugFirstPlayerName(): String? = runBlocking {
    buildAppDatabase(getDatabaseBuilder()).playerSeasonDao().getAllList().firstOrNull()?.player
}
