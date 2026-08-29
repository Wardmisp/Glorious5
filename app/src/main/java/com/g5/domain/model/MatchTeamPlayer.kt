package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchTeamPlayer(
    val id: String,
    @SerialName("match_team_id") val matchTeamId: String,
    @SerialName("nba_player_id") val nbaPlayerId: Int,
    @SerialName("price_paid") val pricePaid: Int = 0,
    @SerialName("won_at") val wonAt: String? = null
)
