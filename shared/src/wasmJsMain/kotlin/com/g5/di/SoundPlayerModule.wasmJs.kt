package com.g5.di

import com.g5.core.utils.NoopSoundPlayer
import com.g5.core.utils.SoundPlayer
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun soundPlayerModule(): Module = module {
    single<SoundPlayer> { NoopSoundPlayer() }
}
