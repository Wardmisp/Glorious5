package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val id: String,
    val name: String? = null,
    @SerialName("player1_id") val player1Id: String,
    @SerialName("player2_id") val player2Id: String? = null,
    val status: String = "waiting", // waiting | drafting | completed | cancelled
    @SerialName("team_size") val teamSize: Int = 5,
    val budget: Int = 50,
    @SerialName("next_opener_id") val nextOpenerId: String? = null,
    @SerialName("next_auto_assign_id") val nextAutoAssignId: String? = null,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
)
