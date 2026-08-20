package com.g5.domain.model

data class GameAction(
    val description: String,
    val highlights: List<NBAPlayer> = emptyList(),
    val favorsTeamA: Boolean = true
)

data class QuarterSimulation(
    val quarterNumber: Int,
    val actions: List<GameAction>
)
