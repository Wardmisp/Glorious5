package com.g5.data.repository

import com.g5.data.local.NBA_PLAYERS
import com.g5.data.local.PlayerSeason
import com.g5.data.local.PlayerSeasonDao
import com.g5.data.remote.dto.NbaPlayerDto
import com.g5.domain.model.NBAPlayer
import com.g5.domain.repository.PlayerRepository
import com.g5.core.utils.TeamColors
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepositoryImpl(
    private val dao: PlayerSeasonDao,
    private val supabaseClient: SupabaseClient
) : PlayerRepository {

    override fun getAllPlayersFlow(): Flow<List<NBAPlayer>> {
        return dao.getAll().map { list -> list.map { it.toNBAPlayer() } }
    }

    override suspend fun getAllPlayers(): List<NBAPlayer> {
        val list = dao.getAllList()
        return if (list.isNotEmpty()) {
            list.map { it.toNBAPlayer() }
        } else {
            NBA_PLAYERS
        }
    }

    override suspend fun getAuctionPlayers(limit: Int): List<NBAPlayer> {
        val list = dao.getRandomPlayers(limit)
        return if (list.isNotEmpty()) {
            list.map { it.toNBAPlayer() }
        } else {
            NBA_PLAYERS.take(limit)
        }
    }

    override suspend fun getPlayerById(id: Int): NBAPlayer? {
        val season = dao.getById(id)
        return season?.toNBAPlayer() ?: NBA_PLAYERS.find { it.id == id }
    }

    override suspend fun getAllSeasons(): List<NBAPlayer> {
        return dao.getAllList().map { it.toNBAPlayer() }
    }

    override suspend fun getSupabaseAuctionPlayers(limit: Int): List<NBAPlayer> {
        return try {
            // Pour avoir du "random" sur Supabase sans extension pgcrypto,
            // on peut soit utiliser une fonction RPC, soit tirer des IDs aléatoires,
            // soit prendre un batch et mélanger localement.
            // Ici on va prendre les 100 premiers (ou un range) et en choisir 10.
            val players = supabaseClient.postgrest["NbaBest1000"]
                .select()
                .decodeList<NbaPlayerDto>()

            players.shuffled().take(limit).map { dto ->
                dto.toDomain().let { player ->
                    player.copy(
                        position = formatPosition(player.position),
                        teamColor = TeamColors.getHexColor(player.team)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback sur le local en cas d'erreur réseau
            getAuctionPlayers(limit)
        }
    }
}

fun PlayerSeason.toNBAPlayer(): NBAPlayer {
    val cleanName = player.trim()
        .replace("ć", "c")
        .replace("Ć", "C")
        .replace("č", "c")
        .replace("Č", "C")
        .replace("š", "s")
        .replace("Š", "S")
        .replace("ž", "z")
        .replace("Ž", "Z")
        .replace("đ", "d")
        .replace("Đ", "D")

    val nameParts = cleanName.split(" ", limit = 2)
    val firstName = nameParts.getOrNull(0) ?: cleanName
    val lastName = nameParts.getOrNull(1) ?: ""
    return NBAPlayer(
        id = id ?: 0,
        firstName = firstName,
        lastName = lastName,
        position = formatPosition(position),
        team = team ?: "NBA",
        teamColor = getTeamColor(team),
        season = season,
        pts = pts ?: 0.0,
        reb = reb ?: 0.0,
        ast = ast ?: 0.0,
        stl = stl ?: 0.0,
        blk = blk ?: 0.0,
        fgPct = fgPct ?: 0.0,
        fg3Pct = fg3Pct ?: 0.0,
        ftPct = ftPct ?: 0.0,
        per = per ?: 0.0,
        winShares = winShares ?: 0.0,
        games = games ?: 0
    )
}
