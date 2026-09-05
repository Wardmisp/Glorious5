package com.g5.di

import com.g5.core.utils.SoundManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { SoundManager(androidContext()) }
}
