package com.g5.di

import org.koin.core.module.Module

/** [PlayerRepository][com.g5.domain.repository.PlayerRepository] est fourni différemment par
 * plateforme : Room sur Android/iOS (voir roomMain), un export JSON en mémoire sur le web (pas de
 * vraie base SQLite dans le navigateur — voir JsonPlayerRepository.kt). */
expect fun repositoryModule(): Module
