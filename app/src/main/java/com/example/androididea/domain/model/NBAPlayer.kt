package com.example.androididea.domain.model

data class NBAPlayer(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val position: String,
    val team: String,
    val teamColor: String,
    val season: String,
    val pts: Double,
    val reb: Double,
    val ast: Double,
    val stl: Double,
    val blk: Double,
    val fgPct: Double = 0.0,
    val fg3Pct: Double = 0.0,
    val ftPct: Double = 0.0,
    val per: Double = 0.0,
    val winShares: Double = 0.0,
    val games: Int = 0
)
