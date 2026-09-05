package com.g5.domain.repository

import com.g5.data.remote.dto.Auction
import com.g5.data.remote.dto.Bid
import com.g5.data.remote.dto.Match
import com.g5.data.remote.dto.MatchTeam
import com.g5.data.remote.dto.MatchTeamPlayer
import com.g5.domain.model.NBAPlayer
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.flow.Flow

/**
 * Toutes les interactions serveur pour le mode multijoueur en ligne
 * (glorious5_multiplayer_schema.sql). Ne touche jamais au pool NbaBest1000
 * ni au mode hotseat local, gérés par [PlayerRepository].
 */
interface MultiplayerRepository {

    /** À appeler une fois avant d'afficher un chrono basé sur une deadline serveur. */
    suspend fun syncClock()

    /** Convertit un timestamp serveur (déjà en millis) en une valeur comparable à System.currentTimeMillis() sur cet appareil. */
    fun toLocalMillis(serverMillis: Long): Long

    suspend fun ensureSignedIn(): String
    fun currentUserId(): String?

    suspend fun createMatch(opponentId: String? = null, budget: Int = 50, teamSize: Int = 5): String
    suspend fun joinMatch(matchId: String)
    suspend fun presentNextPlayer(matchId: String): String?
    suspend fun expireTurnIfOverdue(auctionId: String)
    suspend fun startTurnClock(auctionId: String)

    suspend fun placeBid(auctionId: String, userId: String, amount: Int)
    suspend fun passBid(auctionId: String, userId: String)

    suspend fun getMatch(matchId: String): Match
    suspend fun getMatchTeams(matchId: String): List<MatchTeam>
    suspend fun getRosters(teamIds: List<String>): List<MatchTeamPlayer>
    suspend fun getAuctionById(auctionId: String): Auction?
    suspend fun getBids(auctionId: String): List<Bid>
    suspend fun getLatestAuction(matchId: String): Auction?
    suspend fun getAllNbaPlayers(): List<NBAPlayer>
    suspend fun getNbaPlayers(ids: List<Int>): List<NBAPlayer>
    suspend fun listOpenMatches(): List<Match>

    fun openMatchChannel(matchId: String): RealtimeChannel
    fun matchChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction>
    fun auctionChanges(channel: RealtimeChannel, matchId: String): Flow<PostgresAction>
    fun rosterChanges(channel: RealtimeChannel, teamIds: List<String>): Flow<PostgresAction>
    suspend fun subscribeChannel(channel: RealtimeChannel)
    suspend fun closeChannel(channel: RealtimeChannel)
}
