package com.g5.ui.viewmodel

import com.g5.domain.model.GameState

enum class Difficulty {
    BEGINNER, NORMAL, DIFFICULT
}

data class UiState(
    val isDarkTheme: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val isTutorialActive: Boolean = false,
    val isFirstLaunch: Boolean = true,
    val tutorialStep: Int = 0,
    val gameState: GameState = GameState()
)
