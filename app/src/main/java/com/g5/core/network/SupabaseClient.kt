package com.g5.core.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://kwpzabxwrtiywmsvrqpl.supabase.co",
        supabaseKey = "sb_publishable_EMDnz0f9HCAPFv09piQCNg_kAOlsWOt"
    ) {
        install(Postgrest)
        //Auth for future usage
        install(Auth)
    }
}
