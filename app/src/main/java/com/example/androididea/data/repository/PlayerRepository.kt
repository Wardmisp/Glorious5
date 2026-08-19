package com.example.androididea.data.repository

import android.content.Context
import com.example.androididea.data.AppDatabase
import com.example.androididea.data.PlayerSeason
import com.example.androididea.data.PlayerSeasonDao
import com.example.androididea.data.models.NBA_PLAYERS
import com.example.androididea.data.models.NBAPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerRepository(private val dao: PlayerSeasonDao) {

    constructor(context: Context) : this(AppDatabase.getInstance(context).playerSeasonDao())

    fun getAllPlayersFlow(): Flow<List<NBAPlayer>> {
        return dao.getAll().map { list -> list.map { it.toNBAPlayer() } }
    }

    suspend fun getAllPlayers(): List<NBAPlayer> {
        val list = dao.getAllList()
        return if (list.isNotEmpty()) {
            list.map { it.toNBAPlayer() }
        } else {
            NBA_PLAYERS
        }
    }

    suspend fun getAuctionPlayers(limit: Int = 10): List<NBAPlayer> {
        val list = dao.getRandomPlayers(limit)
        return if (list.isNotEmpty()) {
            list.map { it.toNBAPlayer() }
        } else {
            NBA_PLAYERS.take(limit)
        }
    }

    suspend fun getPlayerById(id: Int): NBAPlayer? {
        val season = dao.getById(id)
        return season?.toNBAPlayer() ?: NBA_PLAYERS.find { it.id == id }
    }
}

fun formatPosition(pos: String?): String {
    if (pos.isNullOrBlank()) return "Polyvalent"
    return when (pos.uppercase().trim()) {
        "PG" -> "Meneur"
        "SG" -> "Arrière"
        "SF" -> "Ailier"
        "PF" -> "Ailier Fort"
        "C" -> "Pivot"
        "G" -> "Arrière"
        "F" -> "Ailier"
        "GF", "G-F", "F-G" -> "Arrière-Ailier"
        "FC", "F-C", "C-F" -> "Intérieur"
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
    val nameParts = player.trim().split(" ", limit = 2)
    val firstName = nameParts.getOrNull(0) ?: player
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
        blk = blk ?: 0.0
    )
}
