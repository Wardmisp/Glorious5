package com.g5.di

import com.g5.core.provider.AndroidStringProvider
import com.g5.core.utils.SoundManager
import com.g5.domain.provider.StringProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { SoundManager(androidContext()) }
    single<StringProvider> { AndroidStringProvider(androidContext()) }
}
