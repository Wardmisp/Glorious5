package com.g5.data.repository

import com.g5.core.network.SupabaseClient
import com.g5.domain.model.Auction
import com.g5.domain.model.BidInsertRequest
import com.g5.domain.model.Match
import com.g5.domain.model.MatchTeam
import com.g5.domain.model.MatchTeamPlayer
import com.g5.domain.model.NBAPlayer
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
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Toutes les interactions Supabase pour le mode multijoueur en ligne
 * (glorious5_multiplayer_schema.sql). Ne touche jamais au pool NbaBest1000
 * ni au mode hotseat local, gérés par [PlayerRepository].
 */
class MultiplayerRepository {
    private val client get() = SupabaseClient.client

    // --- Auth (anonyme) ---

    /**
     * Restaure ou crée une session Supabase anonyme. Idempotent : ne recrée
     * pas d'utilisateur si une session est déjà persistée sur l'appareil.
     */
    suspend fun ensureSignedIn(): String {
        client.auth.awaitInitialization()
        if (client.auth.currentUserOrNull() == null) {
            client.auth.signInAnonymously()
        }
        return client.auth.currentUserOrNull()?.id
            ?: error("Impossible de créer une session Supabase")
    }

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    // --- RPC ---

    @Serializable
    private data class CreateMatchParams(
        @SerialName("p_opponent_id") val opponentId: String? = null,
        @SerialName("p_budget") val budget: Int = 50,
        @SerialName("p_team_size") val teamSize: Int = 5
    )

    suspend fun createMatch(opponentId: String? = null, budget: Int = 50, teamSize: Int = 5): String =
        client.postgrest.rpc("create_match", CreateMatchParams(opponentId, budget, teamSize)).decodeAs<String>()

    @Serializable
    private data class MatchIdParam(@SerialName("p_match_id") val matchId: String)

    suspend fun joinMatch(matchId: String) {
        client.postgrest.rpc("join_match", MatchIdParam(matchId))
    }

    /** Retourne l'id de l'enchère créée, ou null si le match est terminé. */
    suspend fun presentNextPlayer(matchId: String): String? =
        client.postgrest.rpc("present_next_player", MatchIdParam(matchId)).decodeAs<String?>()

    // --- Mises ---

    suspend fun placeBid(auctionId: String, userId: String, amount: Int) {
        client.postgrest["bids"].insert(BidInsertRequest(auctionId, userId, amount))
    }

    suspend fun passBid(auctionId: String, userId: String) {
        client.postgrest["bids"].insert(BidInsertRequest(auctionId, userId, amount = null))
    }

    // --- Hydratation (toujours appelée avant d'ouvrir le realtime) ---

    suspend fun getMatch(matchId: String): Match =
        client.postgrest["matches"].select { filter { eq("id", matchId) } }.decodeSingle()

    suspend fun getMatchTeams(matchId: String): List<MatchTeam> =
        client.postgrest["match_teams"].select { filter { eq("match_id", matchId) } }.decodeList()

    suspend fun getRosters(teamIds: List<String>): List<MatchTeamPlayer> {
        if (teamIds.isEmpty()) return emptyList()
        return client.postgrest["match_team_players"].select {
            filter { filter("match_team_id", FilterOperator.IN, teamIds) }
        }.decodeList()
    }

    suspend fun getAuctionById(auctionId: String): Auction? =
        client.postgrest["auctions"].select { filter { eq("id", auctionId) } }.decodeList<Auction>().firstOrNull()

    suspend fun getActiveAuction(matchId: String): Auction? =
        client.postgrest["auctions"].select {
            filter {
                eq("match_id", matchId)
                eq("status", "active")
            }
            order("created_at", Order.DESCENDING)
            limit(1)
        }.decodeList<Auction>().firstOrNull()

    /** Va chercher les joueurs NBA (table NbaBest1000) référencés par une enchère ou un roster. */
    suspend fun getNbaPlayers(ids: List<Int>): List<NBAPlayer> {
        if (ids.isEmpty()) return emptyList()
        return client.postgrest["NbaBest1000"].select {
            filter { filter("id", FilterOperator.IN, ids) }
        }.decodeList<NBAPlayer>().map { player ->
            player.copy(position = formatPosition(player.position), teamColor = getTeamColor(player.team))
        }
    }

    suspend fun listOpenMatches(): List<Match> =
        client.postgrest["matches"].select {
            filter {
                eq("status", "waiting")
                filter("player2_id", FilterOperator.IS, null)
            }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    // --- Realtime : un channel par match, plusieurs flows dessus ---

    fun openMatchChannel(matchId: String): RealtimeChannel =
        client.realtime.channel("match-$matchId")

    fun matchChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "matches"
            filter("id", FilterOperator.EQ, matchId)
        }

    fun auctionChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "auctions"
            filter("match_id", FilterOperator.EQ, matchId)
        }

    fun rosterChanges(channel: RealtimeChannel, teamIds: List<String>): Flow<PostgresAction> =
        channel.postgresChangeFlow(schema = "public") {
            table = "match_team_players"
            filter("match_team_id", FilterOperator.IN, teamIds)
        }

    suspend fun subscribeChannel(channel: RealtimeChannel) {
        channel.subscribe()
    }

    suspend fun closeChannel(channel: RealtimeChannel) {
        channel.unsubscribe()
        client.realtime.removeChannel(channel)
    }
}
