package com.g5.di

import org.koin.core.module.Module
import org.koin.dsl.module

/** Rien à fournir : le web n'a pas de base Room (voir RepositoryModule.wasmJs.kt), cette cible
 * n'a donc pas besoin d'un `AppDatabase`/`PlayerSeasonDao`. */
actual fun databaseModule(): Module = module {}
