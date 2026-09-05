package com.g5.data.remote.dto

import com.g5.core.network.FlexibleDoubleSerializer
import com.g5.domain.model.NBAPlayer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reflet 1:1 de la table `NbaBest1000` côté Supabase — seule la sérialisation JSON en dépend,
 * jamais le domaine (voir [toDomain]). */
@Serializable
data class NbaPlayerDto(
    val id: Int,
    @SerialName("player") val fullName: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    val position: String,
    val team: String,
    @SerialName("team_color") val teamColor: String = "#F4722B",
    val season: String,
    val pts: Double,
    val reb: Double,
    val ast: Double,
    @Serializable(with = FlexibleDoubleSerializer::class) val stl: Double = 0.0,
    @Serializable(with = FlexibleDoubleSerializer::class) val blk: Double = 0.0,
    @SerialName("fg_pct") val fgPct: Double = 0.0,
    @SerialName("fg3_pct") @Serializable(with = FlexibleDoubleSerializer::class) val fg3Pct: Double = 0.0,
    @SerialName("ft_pct") val ftPct: Double = 0.0,
    val per: Double = 0.0,
    @SerialName("win_shares") val winShares: Double = 0.0,
    val games: Int = 0
) {
    fun toDomain(): NBAPlayer = NBAPlayer(
        id = id,
        fullName = fullName,
        firstName = firstName,
        lastName = lastName,
        position = position,
        team = team,
        teamColor = teamColor,
        season = season,
        pts = pts,
        reb = reb,
        ast = ast,
        stl = stl,
        blk = blk,
        fgPct = fgPct,
        fg3Pct = fg3Pct,
        ftPct = ftPct,
        per = per,
        winShares = winShares,
        games = games
    )
}
