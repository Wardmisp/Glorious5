package com.g5.di

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

/** Démarre Koin une seule fois (idempotent) — il n'y a pas d'équivalent à
 * `Application.onCreate()` côté iOS pour le faire en amont une bonne fois pour toutes, donc
 * chaque point d'entrée (UI, tests) s'en assure lui-même au premier accès.
 * `KoinPlatformTools.defaultContext()` plutôt que `GlobalContext` directement : c'est l'API
 * multiplateforme recommandée par Koin, `GlobalContext` seul ne résout pas sur Kotlin/Native. */
fun ensureKoinStarted(): Koin =
    KoinPlatformTools.defaultContext().getOrNull() ?: startKoin { modules(appKoinModules()) }.koin
