package com.g5.di

import com.g5.data.local.AppDatabase
import com.g5.data.local.PlayerSeasonDao
import com.g5.data.local.buildAppDatabase
import com.g5.data.local.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun databaseModule(): Module = module {
    single { buildAppDatabase(getDatabaseBuilder()) }
    single<PlayerSeasonDao> { get<AppDatabase>().playerSeasonDao() }
}
