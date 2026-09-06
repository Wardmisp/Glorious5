package com.g5.domain.repository

import com.g5.domain.model.NBAPlayer
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayersFlow(): Flow<List<NBAPlayer>>
    suspend fun getAllPlayers(): List<NBAPlayer>
    suspend fun getAuctionPlayers(limit: Int = 10): List<NBAPlayer>
    suspend fun getPlayerById(id: Int): NBAPlayer?
    suspend fun getAllSeasons(): List<NBAPlayer>
    suspend fun getSupabaseAuctionPlayers(limit: Int = 10): List<NBAPlayer>
}
