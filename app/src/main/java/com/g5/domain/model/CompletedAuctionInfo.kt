package com.g5.domain.model

/**
 * Enchère qui vient de se conclure, affichée en "tampon" (joueur pleinement révélé + qui l'a
 * remporté) avant de passer à la suivante — évite que l'écran change brusquement de joueur.
 */
data class CompletedAuctionInfo(
    val auctionId: String,
    val player: NBAPlayer,
    val winnerIsMe: Boolean,
    val pricePaid: Int,
    val isAutoAssigned: Boolean,
    val isLastPick: Boolean
)
