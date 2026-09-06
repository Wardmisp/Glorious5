package com.g5.di

import com.g5.ui.viewmodel.GameViewModel
import com.g5.ui.viewmodel.MultiplayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        GameViewModel(
            playerRepository = get(),
            soundManager = get(),
            auctionUseCase = get(),
            computerBidUseCase = get(),
            calculateWinProbabilityUseCase = get(),
            generateMatchSimulationUseCase = get(),
            buildTutorialDemoUseCase = get()
        )
    }
    viewModel {
        MultiplayerViewModel(
            repository = get(),
            calculateWinProbabilityUseCase = get(),
            generateMatchSimulationUseCase = get(),
            resolveCompletedAuctionUseCase = get()
        )
    }
}
