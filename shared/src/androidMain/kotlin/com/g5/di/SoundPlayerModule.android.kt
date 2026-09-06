package com.g5.di

import com.g5.core.utils.SoundManager
import com.g5.core.utils.SoundPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun soundPlayerModule(): Module = module {
    single<SoundPlayer> { SoundManager(androidContext()) }
}
