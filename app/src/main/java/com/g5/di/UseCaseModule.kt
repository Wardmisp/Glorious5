package com.g5.di

import com.g5.domain.usecase.AuctionUseCase
import com.g5.domain.usecase.BuildTutorialDemoUseCase
import com.g5.domain.usecase.CalculateWinProbabilityUseCase
import com.g5.domain.usecase.ComputerBidUseCase
import com.g5.domain.usecase.GenerateMatchSimulationUseCase
import com.g5.domain.usecase.ResolveCompletedAuctionUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { AuctionUseCase() }
    factory { ComputerBidUseCase() }
    factory { CalculateWinProbabilityUseCase() }
    factory { GenerateMatchSimulationUseCase(stringProvider = get()) }
    factory { ResolveCompletedAuctionUseCase() }
    factory { BuildTutorialDemoUseCase(calculateWinProbabilityUseCase = get(), generateMatchSimulationUseCase = get()) }
}
