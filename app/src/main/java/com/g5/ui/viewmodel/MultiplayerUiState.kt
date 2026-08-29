package com.g5.ui.viewmodel

import com.g5.domain.model.Auction
import com.g5.domain.model.Match
import com.g5.domain.model.MatchTeam
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.QuarterSimulation
import com.g5.domain.model.TeamAnalytics
import com.g5.domain.model.TeamEntry

sealed class MultiplayerScreen {
    object Lobby : MultiplayerScreen()
    object InMatch : MultiplayerScreen()
    object Scouting : MultiplayerScreen()
    object Simulation : MultiplayerScreen()
    object Result : MultiplayerScreen()
}

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

data class LobbyUiState(
    val isLoading: Boolean = false,
    val openMatches: List<Match> = emptyList(),
    val joinCodeInput: String = "",
    val budgetInput: Int = 50,
    val teamSizeInput: Int = 5,
    val error: String? = null
)

data class MatchUiState(
    val myUserId: String = "",
    val match: Match? = null,
    val myTeam: MatchTeam? = null,
    val opponentTeam: MatchTeam? = null,
    val myRoster: List<TeamEntry> = emptyList(),
    val opponentRoster: List<TeamEntry> = emptyList(),
    val currentAuction: Auction? = null,
    val currentPlayer: NBAPlayer? = null,
    val bidCount: Int = 0,
    val turnDeadlineAtMillis: Long? = null,
    val isMyTurn: Boolean = false,
    val bidInput: Int = 1,
    val isSubmittingBid: Boolean = false,
    val isRealtimeConnected: Boolean = true,
    val pendingResult: CompletedAuctionInfo? = null,
    val lastDismissedAuctionId: String? = null,
    val analytics: Pair<TeamAnalytics, TeamAnalytics>? = null,
    val matchSimulation: List<QuarterSimulation> = emptyList(),
    val currentSimulationQuarter: Int = 0,
    val error: String? = null
) {
    val canPass: Boolean get() = currentAuction?.currentBidderId != null
}

data class MultiplayerUiState(
    val screen: MultiplayerScreen = MultiplayerScreen.Lobby,
    val lobby: LobbyUiState = LobbyUiState(),
    val match: MatchUiState = MatchUiState()
)
