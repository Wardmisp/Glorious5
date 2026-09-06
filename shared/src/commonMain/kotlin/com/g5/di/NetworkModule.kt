package com.g5.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module

private const val SUPABASE_URL = "https://kwpzabxwrtiywmsvrqpl.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_EMDnz0f9HCAPFv09piQCNg_kAOlsWOt"

/** Moteur HTTP Ktor par plateforme (OkHttp sur Android, Darwin sur iOS) — seule différence entre
 * les deux, voir NetworkModule.android.kt / NetworkModule.ios.kt. */
expect fun httpEngine(): HttpClientEngine

val networkModule = module {
    single<SupabaseClient> {
        createSupabaseClient(supabaseUrl = SUPABASE_URL, supabaseKey = SUPABASE_KEY) {
            httpEngine = httpEngine()
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
