package com.g5.domain.model

data class GameAction(
    val description: String,
    val highlights: List<NBAPlayer> = emptyList(),
    val favorsTeamA: Boolean = true,
    val timeSeconds: Int = 720 // Secondes restantes dans le quart-temps (12:00 = 720)
)

data class QuarterSimulation(
    val quarterNumber: Int,
    val actions: List<GameAction>
)
