package com.g5.domain.model

/** Modèle domaine pur : ni Room ni Supabase ne le décodent directement (voir [com.g5.data.remote.dto.NbaPlayerDto]
 * et [com.g5.data.local.PlayerSeason], mappés vers ce type). */
data class NBAPlayer(
    val id: Int,
    val fullName: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val position: String,
    val team: String,
    val teamColor: String = "#F4722B",
    val season: String,
    val pts: Double,
    val reb: Double,
    val ast: Double,
    val stl: Double = 0.0,
    val blk: Double = 0.0,
    val fgPct: Double = 0.0,
    val fg3Pct: Double = 0.0,
    val ftPct: Double = 0.0,
    val per: Double = 0.0,
    val winShares: Double = 0.0,
    val games: Int = 0
) {
    val displayFirstName: String get() = if (firstName.isNotEmpty()) firstName else fullName.split(" ").firstOrNull() ?: ""
    val displayLastName: String get() = if (lastName.isNotEmpty()) lastName else fullName.split(" ").lastOrNull() ?: ""
}
