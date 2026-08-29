package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MatchTeam(
    val id: String,
    @SerialName("match_id") val matchId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("budget_remaining") val budgetRemaining: Int,
    @SerialName("total_score") val totalScore: Double? = null,
    @SerialName("created_at") val createdAt: String? = null
)
