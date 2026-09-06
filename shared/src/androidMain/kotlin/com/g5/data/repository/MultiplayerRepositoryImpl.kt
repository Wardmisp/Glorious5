package com.g5.data.repository

import com.g5.data.remote.dto.Auction
import com.g5.data.remote.dto.Bid
import com.g5.data.remote.dto.BidInsertRequest
import com.g5.data.remote.dto.Match
import com.g5.data.remote.dto.MatchTeam
import com.g5.data.remote.dto.MatchTeamPlayer
import com.g5.data.remote.dto.NbaPlayerDto
import com.g5.domain.model.NBAPlayer
import com.g5.domain.repository.MultiplayerRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Doit rester en phase avec la même limite appliquée côté SQL dans join_match(). */
private const val LOBBY_MATCH_TTL_SECONDS = 10L * 60L

class MultiplayerRepositoryImpl(
    private val client: SupabaseClient
) : MultiplayerRepository {

    /**
     * Décalage entre l'horloge de cet appareil et celle du serveur (estimé une fois via l'en-tête
     * HTTP Date d'une réponse Supabase). Un chrono basé sur turn_deadline (timestamp serveur) et
     * comparé à l'horloge locale brute serait faux en continu — pas juste "en retard" — sur un
     * appareil dont l'heure système est décalée (courant sur émulateur, ou horloge mal réglée).
     */
    @Volatile
    private var clockOffsetMillis: Long = 0L

    override suspend fun syncClock() {
        try {
            val localBefore = System.currentTimeMillis()
            val result = client.postgrest["matches"].select { limit(1) }
            val localAfter = System.currentTimeMillis()
            val dateHeader = result.headers[HttpHeaders.Date] ?: return
            val serverMillis = ZonedDateTime.parse(dateHeader, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant().toEpochMilli()
            clockOffsetMillis = serverMillis - (localBefore + localAfter) / 2
        } catch (e: Exception) {
            clockOffsetMillis = 0L
        }
    }

    override fun toLocalMillis(serverMillis: Long): Long = serverMillis - clockOffsetMillis

    // --- Auth (anonyme) ---

    override suspend fun ensureSignedIn(): String {
        client.auth.awaitInitialization()
        if (client.auth.currentUserOrNull() == null) {
            client.auth.signInAnonymously()
        }
        return client.auth.currentUserOrNull()?.id
            ?: error("Impossible de créer une session Supabase")
    }

    override fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    // --- RPC ---

    @Serializable
    private data class CreateMatchParams(
        @SerialName("p_opponent_id") val opponentId: String? = null,
        @SerialName("p_budget") val budget: Int = 50,
        @SerialName("p_team_size") val teamSize: Int = 5
    )

    override suspend fun createMatch(opponentId: String?, budget: Int, teamSize: Int): String =
        client.postgrest.rpc("create_match", CreateMatchParams(opponentId, budget, teamSize)).decodeAs<String>()

    @Serializable
    private data class MatchIdParam(@SerialName("p_match_id") val matchId: String)

    override suspend fun joinMatch(matchId: String) {
        client.postgrest.rpc("join_match", MatchIdParam(matchId))
    }

    override suspend fun presentNextPlayer(matchId: String): String? =
        client.postgrest.rpc("present_next_player", MatchIdParam(matchId)).decodeAs<String?>()

    @Serializable
    private data class AuctionIdParam(@SerialName("p_auction_id") val auctionId: String)

    override suspend fun expireTurnIfOverdue(auctionId: String) {
        client.postgrest.rpc("expire_turn_if_overdue", AuctionIdParam(auctionId))
    }

    override suspend fun startTurnClock(auctionId: String) {
        client.postgrest.rpc("start_turn_clock", AuctionIdParam(auctionId))
    }

    // --- Mises ---

    override suspend fun placeBid(auctionId: String, userId: String, amount: Int) {
        client.postgrest["bids"].insert(BidInsertRequest(auctionId, userId, amount))
    }

    override suspend fun passBid(auctionId: String, userId: String) {
        client.postgrest["bids"].insert(BidInsertRequest(auctionId, userId, amount = null))
    }

    // --- Hydratation (toujours appelée avant d'ouvrir le realtime) ---

    override suspend fun getMatch(matchId: String): Match =
        client.postgrest["matches"].select { filter { eq("id", matchId) } }.decodeSingle()

    override suspend fun getMatchTeams(matchId: String): List<MatchTeam> =
        client.postgrest["match_teams"].select { filter { eq("match_id", matchId) } }.decodeList()

    override suspend fun getRosters(teamIds: List<String>): List<MatchTeamPlayer> {
        if (teamIds.isEmpty()) return emptyList()
        return client.postgrest["match_team_players"].select {
            filter { filter("match_team_id", FilterOperator.IN, teamIds) }
        }.decodeList()
    }

    override suspend fun getAuctionById(auctionId: String): Auction? =
        client.postgrest["auctions"].select { filter { eq("id", auctionId) } }.decodeList<Auction>().firstOrNull()

    override suspend fun getBids(auctionId: String): List<Bid> =
        client.postgrest["bids"].select { filter { eq("auction_id", auctionId) } }.decodeList()

    override suspend fun getLatestAuction(matchId: String): Auction? =
        client.postgrest["auctions"].select {
            filter {
                eq("match_id", matchId)
            }
            order("created_at", Order.DESCENDING)
            limit(1)
        }.decodeList<Auction>().firstOrNull()

    override suspend fun getAllNbaPlayers(): List<NBAPlayer> =
        client.postgrest["NbaBest1000"].select().decodeList<NbaPlayerDto>().map { applyDisplayFields(it.toDomain()) }

    override suspend fun getNbaPlayers(ids: List<Int>): List<NBAPlayer> {
        if (ids.isEmpty()) return emptyList()
        return client.postgrest["NbaBest1000"].select {
            filter { filter("id", FilterOperator.IN, ids) }
        }.decodeList<NbaPlayerDto>().map { applyDisplayFields(it.toDomain()) }
    }

    /**
     * La table NbaBest1000 n'expose que le nom complet ("player"), pas first_name/last_name :
     * `firstName`/`lastName` restent vides après décodage, ce qui casse tout code qui les lit
     * directement (GenerateMatchSimulationUseCase, par ex. — displayFirstName/displayLastName
     * ont bien un fallback, mais pas les champs bruts). On les dérive ici une fois pour toutes.
     */
    private fun applyDisplayFields(player: NBAPlayer): NBAPlayer {
        val withColors = player.copy(
            position = formatPosition(player.position),
            teamColor = getTeamColor(player.team)
        )
        if (withColors.firstName.isNotBlank() || withColors.lastName.isNotBlank()) return withColors

        val cleanName = withColors.fullName.trim()
            .replace("ć", "c").replace("Ć", "C")
            .replace("č", "c").replace("Č", "C")
            .replace("š", "s").replace("Š", "S")
            .replace("ž", "z").replace("Ž", "Z")
            .replace("đ", "d").replace("Đ", "D")
        val parts = cleanName.split(" ", limit = 2)
        return withColors.copy(
            firstName = parts.getOrNull(0) ?: cleanName,
            lastName = parts.getOrNull(1) ?: ""
        )
    }

    override suspend fun listOpenMatches(): List<Match> {
        val cutoffIso = Instant.now().minusSeconds(LOBBY_MATCH_TTL_SECONDS).toString()
        return client.postgrest["matches"].select {
            filter {
                eq("status", "waiting")
                filter("player2_id", FilterOperator.IS, null)
                filter("created_at", FilterOperator.GTE, cutoffIso)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList()
    }

    // --- Realtime : un channel par match, plusieurs flows dessus ---

    override fun openMatchChannel(matchId: String): RealtimeChannel =
        client.realtime.channel("match-$matchId")

    override fun matchChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "matches"
            filter("id", FilterOperator.EQ, matchId)
        }

    override fun auctionChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "auctions"
            filter("match_id", FilterOperator.EQ, matchId)
        }

    override fun rosterChanges(channel: RealtimeChannel, teamIds: List<String>): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "match_team_players"
            filter("match_team_id", FilterOperator.IN, teamIds)
        }

    override suspend fun subscribeChannel(channel: RealtimeChannel) {
        channel.subscribe()
    }

    override suspend fun closeChannel(channel: RealtimeChannel) {
        channel.unsubscribe()
        client.realtime.removeChannel(channel)
    }
}
