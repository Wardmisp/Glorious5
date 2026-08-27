package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NBAPlayer(
    val id: Int,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val position: String,
    val team: String,
    @SerialName("team_color") val teamColor: String,
    val season: String,
    val pts: Double,
    val reb: Double,
    val ast: Double,
    val stl: Double,
    val blk: Double,
    @SerialName("fg_pct") val fgPct: Double = 0.0,
    @SerialName("fg3_pct") val fg3Pct: Double = 0.0,
    @SerialName("ft_pct") val ftPct: Double = 0.0,
    val per: Double = 0.0,
    @SerialName("win_shares") val winShares: Double = 0.0,
    val games: Int = 0
)
