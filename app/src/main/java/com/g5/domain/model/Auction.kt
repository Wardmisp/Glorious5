package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Auction(
    val id: String,
    @SerialName("match_id") val matchId: String,
    @SerialName("nba_player_id") val nbaPlayerId: Int,
    @SerialName("auction_type") val auctionType: String = "bid", // bid | auto_assign
    val status: String = "active", // active | completed
    @SerialName("current_bid") val currentBid: Int = 0,
    @SerialName("current_bidder_id") val currentBidderId: String? = null,
    @SerialName("turn_user_id") val turnUserId: String,
    @SerialName("winner_id") val winnerId: String? = null,
    @SerialName("final_price") val finalPrice: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
)
