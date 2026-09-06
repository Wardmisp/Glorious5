package com.g5.data.repository

import com.g5.data.local.NBA_PLAYERS
import com.g5.data.remote.dto.NbaPlayerDto
import com.g5.domain.model.NBAPlayer
import com.g5.domain.repository.PlayerRepository
import com.g5.core.utils.TeamColors
import com.g5.shared.resources.Res
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Ligne brute telle qu'exportée depuis `nba_top300.db` en JSON (voir shared/README ou la
 * commande sqlite3 utilisée lors du portage web) — mêmes champs que
 * [com.g5.data.local.PlayerSeason] (Room, Android/iOS uniquement) mais sans annotation Room, pour
 * rester compilable sur la cible web. */
@Serializable
private data class PlayerSeasonJson(
    val id: Int?,
    val player: String,
    val season: String,
    val team: String? = null,
    val position: String? = null,
    val age: Int? = null,
    val games: Int? = null,
    val minutesPerGame: Double? = null,
    val pts: Double? = null,
    val reb: Double? = null,
    val ast: Double? = null,
    val stl: Double? = null,
    val blk: Double? = null,
    val fgPct: Double? = null,
    val fg3Pct: Double? = null,
    val ftPct: Double? = null,
    val per: Double? = null,
    val winShares: Double? = null,
    val compositeScore: Double? = null
)

private fun PlayerSeasonJson.toNBAPlayer(): NBAPlayer {
    val (firstName, lastName) = cleanPlayerName(player)
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

/**
 * Équivalent web de [PlayerRepositoryImpl][com.g5.data.repository.PlayerRepositoryImpl] (Room,
 * Android/iOS) : la base de joueurs NBA est une donnée de référence en lecture seule, ça ne
 * justifie pas une vraie base SQLite dans le navigateur (voir la discussion sur Room 3.0/OPFS
 * dans le plan de portage web). Les ~300 lignes sont chargées une fois en mémoire depuis un export
 * JSON du même `nba_top300.db` utilisé par Android/iOS, et les quelques requêtes du DAO Room sont
 * répliquées ici en Kotlin pur.
 */
class JsonPlayerRepository(
    private val supabaseClient: SupabaseClient
) : PlayerRepository {

    private var cache: List<PlayerSeasonJson>? = null

    private suspend fun loadAll(): List<PlayerSeasonJson> {
        cache?.let { return it }
        val bytes = Res.readBytes("files/player_seasons.json")
        val decoded = Json.decodeFromString<List<PlayerSeasonJson>>(bytes.decodeToString())
        cache = decoded
        return decoded
    }

    override fun getAllPlayersFlow(): Flow<List<NBAPlayer>> = flow {
        emit(loadAll().map { it.toNBAPlayer() })
    }

    override suspend fun getAllPlayers(): List<NBAPlayer> {
        val list = loadAll()
        return if (list.isNotEmpty()) list.map { it.toNBAPlayer() } else NBA_PLAYERS
    }

    override suspend fun getAuctionPlayers(limit: Int): List<NBAPlayer> {
        val list = loadAll()
        return if (list.isNotEmpty()) {
            list.shuffled().take(limit).map { it.toNBAPlayer() }
        } else {
            NBA_PLAYERS.take(limit)
        }
    }

    override suspend fun getPlayerById(id: Int): NBAPlayer? {
        return loadAll().find { it.id == id }?.toNBAPlayer() ?: NBA_PLAYERS.find { it.id == id }
    }

    override suspend fun getAllSeasons(): List<NBAPlayer> {
        return loadAll().map { it.toNBAPlayer() }
    }

    override suspend fun getSupabaseAuctionPlayers(limit: Int): List<NBAPlayer> {
        return try {
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
            getAuctionPlayers(limit)
        }
    }
}
