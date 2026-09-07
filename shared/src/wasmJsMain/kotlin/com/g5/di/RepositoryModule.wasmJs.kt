package com.g5.di

import com.g5.data.repository.JsonPlayerRepository
import com.g5.data.repository.MultiplayerRepositoryImpl
import com.g5.domain.repository.MultiplayerRepository
import com.g5.domain.repository.PlayerRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun repositoryModule(): Module = module {
    single<PlayerRepository> { JsonPlayerRepository(supabaseClient = get()) }
    single<MultiplayerRepository> { MultiplayerRepositoryImpl(client = get()) }
}
