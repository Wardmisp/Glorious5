package com.example.androididea.data.repository

import com.example.androididea.data.models.NBA_PLAYERS
import com.example.androididea.data.models.NBAPlayer

class PlayerRepository {
    fun getAllPlayers(): List<NBAPlayer> = NBA_PLAYERS
    
    fun getPlayerById(id: Int): NBAPlayer? = NBA_PLAYERS.find { it.id == id }
}
