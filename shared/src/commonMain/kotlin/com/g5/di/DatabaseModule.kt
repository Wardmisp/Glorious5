package com.g5.di

import org.koin.core.module.Module

/** Implémentation par plateforme : construit l'[com.g5.data.local.AppDatabase] à partir d'un
 * `RoomDatabase.Builder` obtenu différemment selon la plateforme (Android a besoin d'un
 * `Context`, pas iOS — voir com.g5.data.local.AppDatabase.android.kt / .ios.kt). */
expect fun databaseModule(): Module
