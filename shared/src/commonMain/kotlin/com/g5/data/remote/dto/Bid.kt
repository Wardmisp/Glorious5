package com.g5.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reflet 1:1 de la table `bids` côté Supabase. */
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
