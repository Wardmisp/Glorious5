package com.g5.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bid(
    val id: String,
    @SerialName("auction_id") val auctionId: String,
    @SerialName("user_id") val userId: String,
    val amount: Int? = null, // null = "passe"
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class BidInsertRequest(
    @SerialName("auction_id") val auctionId: String,
    @SerialName("user_id") val userId: String,
    val amount: Int? = null
)
