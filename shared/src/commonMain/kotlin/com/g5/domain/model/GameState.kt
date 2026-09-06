package com.g5.domain.model

/** État complet d'une partie locale (IA ou hotseat) — pur, sans dépendance UI : c'est ce que
 * font évoluer [com.g5.domain.usecase.AuctionUseCase] et [com.g5.domain.usecase.ComputerBidUseCase]. */
data class GameState(
    val round: Int = 0,
    val bid: Int = 0,
    val bidder: Int? = null,
    val p1Input: Int = 1,
    val p2Input: Int = 1,
    val done: Boolean = false,
    val awardedTo: Int? = null,
    val budgets: Pair<Int, Int> = Pair(50, 50),
    val teams: Pair<List<TeamEntry>, List<TeamEntry>> = Pair(emptyList(), emptyList()),
    val showTeams: Boolean = false,
    val showP1Team: Boolean = false,
    val showP2Team: Boolean = false,
    val gameOver: Boolean = false,
    val thinking: Boolean = false,
    val bidCount: Int = 0,
    val revealOrder: List<Int> = (0..9).toList(),
    val timer: Int = 15,
    val players: List<NBAPlayer> = emptyList(),
    val analytics: Pair<TeamAnalytics, TeamAnalytics>? = null,
    val luckyWinner: Int? = null,
    val matchSimulation: List<QuarterSimulation> = emptyList(),
    val currentSimulationQuarter: Int = 0,
    val activePlayerTurn: Int = 1,
    val isVsHuman: Boolean = false
)
