package com.g5.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Double de test en mémoire de [PlayerSeasonDao] — pas de vraie base Room, pour garder les
 * tests des repositories rapides et hors du framework Android. */
class FakePlayerSeasonDao(private val seasons: List<PlayerSeason> = emptyList()) : PlayerSeasonDao {

    override fun getAll(): Flow<List<PlayerSeason>> = flowOf(seasons.sortedByDescending { it.compositeScore })

    override suspend fun getAllList(): List<PlayerSeason> = seasons

    override suspend fun getRandomPlayers(limit: Int): List<PlayerSeason> = seasons.shuffled().take(limit)

    override suspend fun getById(id: Int): PlayerSeason? = seasons.find { it.id == id }

    override fun searchByPlayer(query: String): Flow<List<PlayerSeason>> =
        flowOf(seasons.filter { it.player.contains(query, ignoreCase = true) })

    override fun getBySeason(season: String): Flow<List<PlayerSeason>> =
        flowOf(seasons.filter { it.season == season })

    override fun getTopScorers(limit: Int): Flow<List<PlayerSeason>> =
        flowOf(seasons.sortedByDescending { it.pts ?: 0.0 }.take(limit))

    override fun getTopPER(limit: Int): Flow<List<PlayerSeason>> =
        flowOf(seasons.sortedByDescending { it.per ?: 0.0 }.take(limit))

    override suspend fun getAllSeasons(): List<String> = seasons.map { it.season }.distinct()
}
