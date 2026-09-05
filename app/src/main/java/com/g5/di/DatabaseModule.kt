package com.g5.di

import com.g5.data.local.AppDatabase
import com.g5.data.local.PlayerSeasonDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single<PlayerSeasonDao> { get<AppDatabase>().playerSeasonDao() }
}
