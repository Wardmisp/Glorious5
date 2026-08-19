package com.example.androididea.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androididea.data.AppDatabase
import com.example.androididea.data.PlayerSeason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlayerSeasonViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).playerSeasonDao()

    val topPlayers: StateFlow<List<PlayerSeason>> =
        dao.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String): Flow<List<PlayerSeason>> = dao.searchByPlayer(query)
}
