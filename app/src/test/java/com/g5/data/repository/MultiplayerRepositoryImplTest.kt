package com.g5.data.repository

import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Teste MultiplayerRepositoryImpl "en intégration" avec un vrai SupabaseClient branché sur un
 * faux moteur HTTP (voir [fakeSupabaseClient]) — seule la couche transport est doublée, tout le
 * reste (construction des requêtes REST/RPC, décodage JSON, calcul de l'horloge) est le vrai code
 * de production.
 *
 * Hors périmètre volontairement : ensureSignedIn/currentUserId (module Auth — nécessiterait de
 * simuler le flux GoTrue complet) et les channels realtime (nécessiteraient un faux moteur
 * WebSocket) — voir la discussion dans la session sur les DTOs pour le même type d'arbitrage.
 */
class MultiplayerRepositoryImplTest {

    private fun unreachableClient() = fakeSupabaseClient { _ ->
        throw java.io.IOException("no network in this test")
    }

    /** HttpRequestData.body.toString() tronque son aperçu du contenu — on va lire le vrai texte. */
    private fun HttpRequestData.bodyText(): String = (body as TextContent).text

    // --- horloge serveur ---

    @Test
    fun syncClock_computesAnOffsetThatConvertsServerTimeBackToLocalTime() = runTest {
        // Horloge serveur délibérément décalée d'une heure : si toLocalMillis ne faisait rien
        // (bug "offset = 0"), le résultat serait à ~1h de maintenant au lieu d'en être proche.
        val serverInstant = Instant.now().plusSeconds(3600)
        val dateHeader = DateTimeFormatter.RFC_1123_DATE_TIME.format(serverInstant.atZone(ZoneOffset.UTC))
        val client = fakeSupabaseClient { _ ->
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    HttpHeaders.Date to listOf(dateHeader)
                )
            )
        }
        val repo = MultiplayerRepositoryImpl(client)

        repo.syncClock()
        val localEquivalent = repo.toLocalMillis(serverInstant.toEpochMilli())

        assertTrue(
            "expected ~${System.currentTimeMillis()}, was $localEquivalent",
            abs(localEquivalent - System.currentTimeMillis()) < 3000
        )
    }

    @Test
    fun syncClock_onFailure_resetsTheOffsetSoToLocalMillisBecomesTheIdentity() = runTest {
        val repo = MultiplayerRepositoryImpl(unreachableClient())

        repo.syncClock() // ne doit pas lancer, juste échouer silencieusement

        assertEquals(123456789L, repo.toLocalMillis(123456789L))
    }

    // --- RPC ---

    @Test
    fun createMatch_decodesTheReturnedMatchIdAndSendsBudgetAndTeamSize() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = fakeSupabaseClient { request ->
            requests.add(request)
            jsonResponse("\"match-123\"")
        }
        val repo = MultiplayerRepositoryImpl(client)

        val matchId = repo.createMatch(opponentId = null, budget = 75, teamSize = 5)

        assertEquals("match-123", matchId)
        assertEquals("/rest/v1/rpc/create_match", requests.single().url.encodedPath)
        assertTrue(requests.single().bodyText().contains("\"p_budget\":75"))
    }

    @Test
    fun presentNextPlayer_decodesANullAuctionId_whenTheMatchIsOver() = runTest {
        val client = fakeSupabaseClient { jsonResponse("null") }
        val repo = MultiplayerRepositoryImpl(client)

        assertNull(repo.presentNextPlayer("match-1"))
    }

    @Test
    fun joinMatch_completesWithoutError_onASuccessfulRpcCall() = runTest {
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse("null") })

        repo.joinMatch("match-1") // ne doit pas lancer
    }

    // --- mises ---

    @Test
    fun placeBid_sendsAnInsertWithTheGivenAmount() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = fakeSupabaseClient { request ->
            requests.add(request)
            jsonResponse("[]")
        }
        val repo = MultiplayerRepositoryImpl(client)

        repo.placeBid(auctionId = "auction-1", userId = "user-1", amount = 20)

        val body = requests.single().bodyText()
        assertTrue(body.contains("auction-1"))
        assertTrue(body.contains("\"amount\":20"))
    }

    @Test
    fun passBid_sendsAnInsertWithANullAmount() = runTest {
        val requests = mutableListOf<HttpRequestData>()
        val client = fakeSupabaseClient { request ->
            requests.add(request)
            jsonResponse("[]")
        }
        val repo = MultiplayerRepositoryImpl(client)

        repo.passBid(auctionId = "auction-1", userId = "user-1")

        // Le JSON encoder du SDK omet les champs null (explicitNulls=false) plutôt que d'écrire
        // "amount":null — on vérifie donc l'absence de toute valeur numérique, pas une valeur pile.
        val body = requests.single().bodyText()
        assertTrue(body.contains("auction-1"))
        assertTrue("did not expect a numeric amount in a pass: $body", !Regex("\"amount\"\\s*:\\s*\\d").containsMatchIn(body))
    }

    // --- hydratation ---

    @Test
    fun getMatch_decodesTheSingleMatchRow() = runTest {
        val json = """[{"id":"match-1","player1_id":"user-1","player2_id":"user-2","status":"drafting","team_size":5,"budget":50}]"""
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val match = repo.getMatch("match-1")

        assertEquals("match-1", match.id)
        assertEquals("drafting", match.status)
        assertEquals(50, match.budget)
    }

    @Test
    fun getMatchTeams_decodesTheListOfTeams() = runTest {
        val json = """[
            {"id":"team-1","match_id":"match-1","user_id":"user-1","budget_remaining":35},
            {"id":"team-2","match_id":"match-1","user_id":"user-2","budget_remaining":40}
        ]""".trimIndent()
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val teams = repo.getMatchTeams("match-1")

        assertEquals(listOf(35, 40), teams.map { it.budgetRemaining })
    }

    @Test
    fun getRosters_shortCircuitsToEmptyList_withoutCallingTheNetwork_whenNoTeamIds() = runTest {
        val repo = MultiplayerRepositoryImpl(unreachableClient())

        assertEquals(emptyList<Any>(), repo.getRosters(emptyList()))
    }

    @Test
    fun getRosters_decodesTheRosterRows() = runTest {
        val json = """[{"id":"row-1","match_team_id":"team-1","nba_player_id":42,"price_paid":17}]"""
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val roster = repo.getRosters(listOf("team-1"))

        assertEquals(17, roster.single().pricePaid)
    }

    @Test
    fun getAuctionById_returnsNull_whenTheResultSetIsEmpty() = runTest {
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse("[]") })

        assertNull(repo.getAuctionById("auction-404"))
    }

    @Test
    fun getAuctionById_decodesTheMatchingAuction() = runTest {
        val json = """[{"id":"auction-1","match_id":"match-1","nba_player_id":42,"turn_user_id":"user-1","current_bid":15}]"""
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val auction = repo.getAuctionById("auction-1")

        assertEquals(15, auction?.currentBid)
    }

    @Test
    fun getBids_decodesAmountsIncludingNullPasses() = runTest {
        val json = """[
            {"id":"bid-1","auction_id":"auction-1","user_id":"user-1","amount":10},
            {"id":"bid-2","auction_id":"auction-1","user_id":"user-2","amount":null}
        ]""".trimIndent()
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val bids = repo.getBids("auction-1")

        assertEquals(listOf(10, null), bids.map { it.amount })
    }

    @Test
    fun getLatestAuction_returnsNull_whenThereIsNoneYet() = runTest {
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse("[]") })

        assertNull(repo.getLatestAuction("match-1"))
    }

    @Test
    fun getAllNbaPlayers_decodesAndAppliesDisplayFormatting() = runTest {
        // NbaBest1000 n'expose que le nom complet : firstName/lastName doivent être dérivés, et la
        // position/couleur d'équipe formatées pour l'affichage (voir applyDisplayFields).
        val json = """[{"id":1,"player":"Nikola Jokic","position":"c","team":"DEN","season":"2023-24","pts":26.0,"reb":12.0,"ast":9.0}]"""
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val player = repo.getAllNbaPlayers().single()

        assertEquals("Nikola", player.firstName)
        assertEquals("Jokic", player.lastName)
        assertEquals("C", player.position) // formatPosition normalise la casse ("c" -> "C")
    }

    @Test
    fun getNbaPlayers_shortCircuitsToEmptyList_withoutCallingTheNetwork_whenNoIds() = runTest {
        val repo = MultiplayerRepositoryImpl(unreachableClient())

        assertEquals(emptyList<Any>(), repo.getNbaPlayers(emptyList()))
    }

    @Test
    fun listOpenMatches_decodesTheWaitingMatches() = runTest {
        val json = """[{"id":"match-1","player1_id":"user-1","status":"waiting"}]"""
        val repo = MultiplayerRepositoryImpl(fakeSupabaseClient { jsonResponse(json) })

        val matches = repo.listOpenMatches()

        assertEquals("waiting", matches.single().status)
    }
}
