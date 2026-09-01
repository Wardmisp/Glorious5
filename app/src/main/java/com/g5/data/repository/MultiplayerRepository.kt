package com.g5.data.repository

import com.g5.core.network.SupabaseClient
import com.g5.domain.model.Auction
import com.g5.domain.model.Bid
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
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Doit rester en phase avec la même limite appliquée côté SQL dans join_match(). */
private const val LOBBY_MATCH_TTL_SECONDS = 10L * 60L

/**
 * Toutes les interactions Supabase pour le mode multijoueur en ligne
 * (glorious5_multiplayer_schema.sql). Ne touche jamais au pool NbaBest1000
 * ni au mode hotseat local, gérés par [PlayerRepository].
 */
class MultiplayerRepository {
    private val client get() = SupabaseClient.client

    /**
     * Décalage entre l'horloge de cet appareil et celle du serveur (estimé une fois via l'en-tête
     * HTTP Date d'une réponse Supabase). Un chrono basé sur turn_deadline (timestamp serveur) et
     * comparé à l'horloge locale brute serait faux en continu — pas juste "en retard" — sur un
     * appareil dont l'heure système est décalée (courant sur émulateur, ou horloge mal réglée).
     */
    @Volatile
    private var clockOffsetMillis: Long = 0L

    /** À appeler une fois avant d'afficher un chrono basé sur une deadline serveur. */
    suspend fun syncClock() {
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

    /** Convertit un timestamp serveur (déjà en millis) en une valeur comparable à System.currentTimeMillis() sur cet appareil. */
    fun toLocalMillis(serverMillis: Long): Long = serverMillis - clockOffsetMillis

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
        @SerialName("p_team_size") val teamSize: Int = 5,
        @SerialName("p_name") val name: String? = null
    )

    suspend fun createMatch(opponentId: String? = null, budget: Int = 50, teamSize: Int = 5, name: String? = null): String =
        client.postgrest.rpc("create_match", CreateMatchParams(opponentId, budget, teamSize, name)).decodeAs<String>()

    @Serializable
    private data class MatchIdParam(@SerialName("p_match_id") val matchId: String)

    suspend fun joinMatch(matchId: String) {
        client.postgrest.rpc("join_match", MatchIdParam(matchId))
    }

    /** Retourne l'id de l'enchère créée, ou null si le match est terminé. */
    suspend fun presentNextPlayer(matchId: String): String? =
        client.postgrest.rpc("present_next_player", MatchIdParam(matchId)).decodeAs<String?>()

    @Serializable
    private data class AuctionIdParam(@SerialName("p_auction_id") val auctionId: String)

    /**
     * Force la résolution d'une enchère dont le délai de 15s est dépassé (mise/passe "au nom" du
     * joueur en retard). Sans danger à appeler spéculativement : no-op côté serveur si l'enchère
     * n'est plus active ou si le délai n'est pas encore dépassé.
     */
    suspend fun expireTurnIfOverdue(auctionId: String) {
        client.postgrest.rpc("expire_turn_if_overdue", AuctionIdParam(auctionId))
    }

    /**
     * Démarre le chrono d'ouverture d'une enchère — à appeler par le joueur qui doit ouvrir,
     * une fois qu'il voit effectivement cette enchère (turn_deadline encore null) à l'écran.
     * Sans effet si le chrono est déjà lancé ou si ce n'est pas à lui d'ouvrir.
     */
    suspend fun startTurnClock(auctionId: String) {
        client.postgrest.rpc("start_turn_clock", AuctionIdParam(auctionId))
    }

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

    /** Historique des mises d'une enchère — sert au compteur de reveal progressif et au chrono
     * (basé sur l'horodatage de la dernière mise, ou de la création de l'enchère si aucune). */
    suspend fun getBids(auctionId: String): List<Bid> =
        client.postgrest["bids"].select { filter { eq("auction_id", auctionId) } }.decodeList()

    /** La toute dernière enchère du match, quel que soit son statut/type — sert à savoir si
     * c'est une enchère en cours à afficher (active) ou une déjà résolue à faire acquitter
     * (bid conclu, passe, timeout, ou attribution automatique) avant de passer à la suite. */
    suspend fun getLatestAuction(matchId: String): Auction? =
        client.postgrest["auctions"].select {
            filter {
                eq("match_id", matchId)
            }
            order("created_at", Order.DESCENDING)
            limit(1)
        }.decodeList<Auction>().firstOrNull()

    /** Pool complet, utilisé comme distribution de référence pour les percentiles du rapport de scouting. */
    suspend fun getAllNbaPlayers(): List<NBAPlayer> =
        client.postgrest["NbaBest1000"].select().decodeList<NBAPlayer>().map { applyDisplayFields(it) }

    /** Va chercher les joueurs NBA (table NbaBest1000) référencés par une enchère ou un roster. */
    suspend fun getNbaPlayers(ids: List<Int>): List<NBAPlayer> {
        if (ids.isEmpty()) return emptyList()
        return client.postgrest["NbaBest1000"].select {
            filter { filter("id", FilterOperator.IN, ids) }
        }.decodeList<NBAPlayer>().map { applyDisplayFields(it) }
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

    /** Une partie en attente disparaît du lobby après ce délai sans adversaire (voir aussi
     * join_match côté SQL, qui applique la même limite pour empêcher de la rejoindre via un
     * code une fois qu'elle a expiré). */
    suspend fun listOpenMatches(): List<Match> {
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
