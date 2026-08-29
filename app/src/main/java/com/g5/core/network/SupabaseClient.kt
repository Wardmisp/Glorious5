package com.g5.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://kwpzabxwrtiywmsvrqpl.supabase.co",
        supabaseKey = "sb_publishable_EMDnz0f9HCAPFv09piQCNg_kAOlsWOt"
    ) {
        httpEngine = OkHttp.create()
        install(Postgrest)
        //Auth for future usage
        install(Auth)
        install(Realtime)
    }
}
