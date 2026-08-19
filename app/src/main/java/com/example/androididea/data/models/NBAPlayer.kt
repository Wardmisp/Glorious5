package com.example.androididea.data.models

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
    val blk: Double
)
