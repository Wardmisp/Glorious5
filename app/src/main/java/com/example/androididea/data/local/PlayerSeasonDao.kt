package com.example.androididea.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerSeasonDao {

    @Query("SELECT * FROM player_seasons ORDER BY composite_score DESC")
    fun getAll(): Flow<List<PlayerSeason>>

    @Query("SELECT * FROM player_seasons ORDER BY composite_score DESC")
    suspend fun getAllList(): List<PlayerSeason>

    @Query("SELECT * FROM player_seasons ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPlayers(limit: Int = 10): List<PlayerSeason>

    @Query("SELECT * FROM player_seasons WHERE id = :id")
    suspend fun getById(id: Int): PlayerSeason?

    @Query("SELECT * FROM player_seasons WHERE player LIKE '%' || :query || '%' " +
           "ORDER BY composite_score DESC")
    fun searchByPlayer(query: String): Flow<List<PlayerSeason>>

    @Query("SELECT * FROM player_seasons WHERE season = :season ORDER BY composite_score DESC")
    fun getBySeason(season: String): Flow<List<PlayerSeason>>

    @Query("SELECT * FROM player_seasons ORDER BY pts DESC LIMIT :limit")
    fun getTopScorers(limit: Int = 10): Flow<List<PlayerSeason>>

    @Query("SELECT * FROM player_seasons ORDER BY per DESC LIMIT :limit")
    fun getTopPER(limit: Int = 10): Flow<List<PlayerSeason>>

    @Query("SELECT DISTINCT season FROM player_seasons ORDER BY season")
    suspend fun getAllSeasons(): List<String>
}
