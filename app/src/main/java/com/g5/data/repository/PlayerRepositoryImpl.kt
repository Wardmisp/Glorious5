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

/**
 * Normalise le poste brut (venant de Room ou de Supabase) vers un code stable indépendant de la
 * langue — PG/SG/SF/PF/C/GF/FC, ou la valeur d'origine si elle est déjà inconnue. Le libellé
 * affiché (et son abréviation) se résout uniquement à l'affichage, via
 * `com.g5.ui.util.positionLabel`/`positionAbbreviation` — jamais ici : ce code est aussi ce sur
 * quoi s'appuient CalculateWinProbabilityUseCase (poids par poste) et
 * GenerateMatchSimulationUseCase (choix des commentaires), qui doivent rester indépendants de la
 * langue d'affichage.
 */
fun formatPosition(pos: String?): String {
    if (pos.isNullOrBlank()) return ""
    return when (pos.uppercase().trim()) {
        "PG" -> "PG"
        "SG", "G" -> "SG"
        "SF", "F" -> "SF"
        "PF" -> "PF"
        "C" -> "C"
        "GF", "G-F", "F-G" -> "GF"
        "FC", "F-C", "C-F" -> "FC"
        else -> pos
    }
}

fun getTeamColor(team: String?): String {
    if (team.isNullOrBlank()) return "#1D428A"
    val t = team.uppercase().trim()
    return when {
        t.contains("ATL") || t.contains("HAWKS") -> "#E03A3E"
        t.contains("BOS") || t.contains("CELTICS") -> "#007A33"
        t.contains("BKN") || t.contains("NJN") || t.contains("NETS") -> "#000000"
        t.contains("CHA") || t.contains("CHO") || t.contains("CHH") || t.contains("HORNETS") || t.contains("BOBCATS") -> "#1D1160"
        t.contains("CHI") || t.contains("BULLS") -> "#CE1141"
        t.contains("CLE") || t.contains("CAVALIERS") || t.contains("CAVS") -> "#860038"
        t.contains("DAL") || t.contains("MAVERICKS") || t.contains("MAVS") -> "#00538C"
        t.contains("DEN") || t.contains("NUGGETS") -> "#0E2240"
        t.contains("DET") || t.contains("PISTONS") -> "#1D428A"
        t.contains("GSW") || t.contains("WARRIORS") -> "#1D428A"
        t.contains("HOU") || t.contains("ROCKETS") -> "#CE1141"
        t.contains("IND") || t.contains("PACERS") -> "#002D62"
        t.contains("LAC") || t.contains("CLIPPERS") -> "#C8102E"
        t.contains("LAL") || t.contains("LAKERS") -> "#552583"
        t.contains("MEM") || t.contains("GRIZZLIES") -> "#5D76A9"
        t.contains("MIA") || t.contains("HEAT") -> "#98002E"
        t.contains("MIL") || t.contains("BUCKS") -> "#00471B"
        t.contains("MIN") || t.contains("TIMBERWOLVES") || t.contains("WOLVES") -> "#0C2340"
        t.contains("NOP") || t.contains("NOH") || t.contains("NOK") || t.contains("PELICANS") -> "#0C2340"
        t.contains("NYK") || t.contains("KNICKS") -> "#006BB6"
        t.contains("OKC") || t.contains("THUNDER") || t.contains("SEA") || t.contains("SONICS") -> "#007AC1"
        t.contains("ORL") || t.contains("MAGIC") -> "#0077C0"
        t.contains("PHI") || t.contains("76ERS") || t.contains("SIXERS") -> "#006BB6"
        t.contains("PHX") || t.contains("PHO") || t.contains("SUNS") -> "#1D1160"
        t.contains("POR") || t.contains("BLAZERS") || t.contains("TRAIL BLAZERS") -> "#E03A3E"
        t.contains("SAC") || t.contains("KINGS") -> "#5A2D81"
        t.contains("SAS") || t.contains("SPURS") -> "#C4CED4"
        t.contains("TOR") || t.contains("RAPTORS") -> "#CE1141"
        t.contains("UTA") || t.contains("JAZZ") -> "#002B5C"
        t.contains("WAS") || t.contains("WIZARDS") || t.contains("BULLETS") -> "#002B5C"
        else -> "#1D428A"
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
