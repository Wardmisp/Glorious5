package com.g5.di

import com.g5.data.repository.MultiplayerRepositoryImpl
import com.g5.data.repository.PlayerRepositoryImpl
import com.g5.domain.repository.MultiplayerRepository
import com.g5.domain.repository.PlayerRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<PlayerRepository> { PlayerRepositoryImpl(dao = get(), supabaseClient = get()) }
    single<MultiplayerRepository> { MultiplayerRepositoryImpl(client = get()) }
}
