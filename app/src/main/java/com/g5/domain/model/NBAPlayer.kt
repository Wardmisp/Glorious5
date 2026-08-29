package com.g5.domain.model

import com.g5.core.network.FlexibleDoubleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NBAPlayer(
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
    val displayFirstName: String get() = if (firstName.isNotEmpty()) firstName else fullName.split(" ").firstOrNull() ?: ""
    val displayLastName: String get() = if (lastName.isNotEmpty()) lastName else fullName.split(" ").lastOrNull() ?: ""
}
