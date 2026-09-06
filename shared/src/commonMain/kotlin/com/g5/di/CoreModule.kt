package com.g5.di

import com.g5.core.provider.CommentaryStringProvider
import com.g5.core.utils.SoundPlayer
import com.g5.domain.provider.StringProvider
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule = module {
    single<StringProvider> { CommentaryStringProvider() }
}

/** [SoundPlayer] a une implémentation différente par plateforme (MediaPlayer sur Android, stub
 * pour l'instant sur iOS) — voir SoundPlayerModule.android.kt / SoundPlayerModule.ios.kt. */
expect fun soundPlayerModule(): Module
