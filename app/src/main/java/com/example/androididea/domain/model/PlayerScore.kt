package com.example.androididea.domain.model

import com.example.androididea.data.models.NBAPlayer

data class PlayerScore(
    val player: NBAPlayer,
    val ptsPercentile: Double,
    val rebPercentile: Double,
    val astPercentile: Double,
    val stlPercentile: Double,
    val blkPercentile: Double,
    val fgPercentile: Double,
    val fg3Percentile: Double,
    val ftPercentile: Double,
    val perPercentile: Double,
    val wsPercentile: Double,
    val effScore: Double,
    val impactScore: Double,
    val totalScore: Double
)

data class TeamAnalytics(
    val teamScore: Double,
    val winProbability: Double,
    val scoredPlayers: List<PlayerScore>
)
