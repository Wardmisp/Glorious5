package com.g5.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val networkModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = "https://kwpzabxwrtiywmsvrqpl.supabase.co",
            supabaseKey = "sb_publishable_EMDnz0f9HCAPFv09piQCNg_kAOlsWOt"
        ) {
            httpEngine = OkHttp.create()
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
