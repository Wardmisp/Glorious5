package com.g5.ui.viewmodel

import com.g5.domain.model.TeamEntry
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TeamAnalytics
import com.g5.domain.model.QuarterSimulation

sealed class Screen {
    object Home : Screen()
    object VsComputer : Screen()
    object VsHuman : Screen()
    object Options : Screen()
    object Simulation : Screen()
}

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
    val gameOver: Boolean = false,
    val thinking: Boolean = false,
    val bidCount: Int = 0,
    val revealOrder: List<Int> = (0..9).toList(),
    val timer: Int = 15,
    val players: List<NBAPlayer> = emptyList(),
    val analytics: Pair<TeamAnalytics, TeamAnalytics>? = null,
    val luckyWinner: Int? = null,
    val matchSimulation: List<QuarterSimulation> = emptyList(),
    val currentSimulationQuarter: Int = 0
)

data class UiState(
    val currentScreen: Screen = Screen.Home,
    val isDarkTheme: Boolean = true,
    val gameState: GameState = GameState()
)
