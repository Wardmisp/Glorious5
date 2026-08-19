package com.example.androididea.viewmodel

import com.example.androididea.data.models.TeamEntry
import com.example.androididea.data.models.NBAPlayer

sealed class Screen {
    object Home : Screen()
    object VsComputer : Screen()
    object VsHuman : Screen()
    object Options : Screen()
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
    val players: List<NBAPlayer> = emptyList()
)

data class UiState(
    val currentScreen: Screen = Screen.Home,
    val gameState: GameState = GameState()
)
