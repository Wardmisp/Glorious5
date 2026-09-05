package com.g5.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

/**
 * Construit un [SupabaseClient] (module Postgrest uniquement) branché sur un faux moteur HTTP :
 * les requêtes REST émises par le SDK sont interceptées par [handler] plutôt que d'aller sur le
 * réseau. Établi empiriquement (voir historique de la session) : le SDK envoie du
 * `GET {url}/rest/v1/{table}?...&select=*` pour les lectures — y compris pour `decodeSingle`, qui
 * attend malgré son nom un corps de réponse enveloppé dans un tableau JSON, comme `decodeList` —
 * du `POST {url}/rest/v1/{table}?columns=...` pour les inserts, et du
 * `POST {url}/rest/v1/rpc/{fonction}` pour les RPC, dont le corps de réponse est la valeur de
 * retour brute (pas enveloppée dans un tableau).
 */
fun fakeSupabaseClient(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): SupabaseClient {
    val engine = MockEngine { request -> handler(request) }
    return createSupabaseClient(
        supabaseUrl = "https://test.supabase.co",
        supabaseKey = "test-anon-key"
    ) {
        httpEngine = engine
        install(Postgrest)
    }
}

fun MockRequestHandleScope.jsonResponse(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
    respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))
