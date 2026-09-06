package com.g5.domain.usecase

import com.g5.data.remote.dto.Auction
import com.g5.domain.model.CompletedAuctionInfo
import com.g5.domain.model.NBAPlayer

/**
 * Décide s'il faut afficher un tampon "joueur remporté" pour la dernière enchère du match — et
 * lequel. Pur : ne fait aucun accès réseau, se contente des données déjà hydratées par
 * [com.g5.ui.viewmodel.MultiplayerViewModel].
 */
class ResolveCompletedAuctionUseCase {

    fun execute(
        latestAuction: Auction?,
        previousPendingResult: CompletedAuctionInfo?,
        lastDismissedAuctionId: String?,
        myId: String,
        players: Map<Int, NBAPlayer>,
        myRosterSize: Int,
        opponentRosterSize: Int,
        teamSize: Int
    ): CompletedAuctionInfo? = when {
        // Déjà affiché : on ne le remplace pas tant que l'utilisateur ne l'a pas fermé.
        previousPendingResult != null -> previousPendingResult

        latestAuction != null && latestAuction.status == "completed" &&
            latestAuction.id != lastDismissedAuctionId -> {
            val winnerId = latestAuction.winnerId
            val player = players[latestAuction.nbaPlayerId]
            if (winnerId != null && player != null) {
                CompletedAuctionInfo(
                    auctionId = latestAuction.id,
                    player = player,
                    winnerIsMe = winnerId == myId,
                    pricePaid = latestAuction.finalPrice ?: 0,
                    isAutoAssigned = latestAuction.auctionType == "auto_assign",
                    isLastPick = myRosterSize >= teamSize && opponentRosterSize >= teamSize
                )
            } else null
        }

        else -> null
    }
}
