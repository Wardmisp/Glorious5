package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuildTutorialDemoUseCaseTest {

    private lateinit var useCase: BuildTutorialDemoUseCase

    private fun players(count: Int): List<NBAPlayer> = (0 until count).map { i ->
        NBAPlayer(
            id = i,
            position = "Pivot",
            team = "GSW",
            season = "2023-24",
            pts = 10.0 + i,
            reb = 5.0 + i,
            ast = 3.0 + i
        )
    }

    @Before
    fun setUp() {
        useCase = BuildTutorialDemoUseCase(
            calculateWinProbabilityUseCase = CalculateWinProbabilityUseCase(),
            generateMatchSimulationUseCase = GenerateMatchSimulationUseCase()
        )
    }

    @Test
    fun execute_withMoreThanTenPlayers_giveAiTheTopFiveAndUserTheBottomFive() {
        val allPlayers = players(12)

        val result = useCase.execute(allPlayers)

        val aiIds = result.teams.second.map { it.player.id }
        val userIds = result.teams.first.map { it.player.id }
        assertEquals(listOf(0, 1, 2, 3, 4), aiIds)
        assertEquals(listOf(7, 8, 9, 10, 11), userIds)
    }

    @Test
    fun execute_withTenOrFewerPlayers_userGetsWhateverIsLeftAfterTheAiPicks() {
        val allPlayers = players(8)

        val result = useCase.execute(allPlayers)

        val aiIds = result.teams.second.map { it.player.id }
        val userIds = result.teams.first.map { it.player.id }
        assertEquals(listOf(0, 1, 2, 3, 4), aiIds)
        assertEquals(listOf(5, 6, 7), userIds)
    }

    @Test
    fun execute_pricesAreFixedDemoValues() {
        val result = useCase.execute(players(12))

        assertTrue(result.teams.first.all { it.paid == 5 })
        assertTrue(result.teams.second.all { it.paid == 45 })
    }

    @Test
    fun execute_alwaysProducesAFourQuarterSimulation() {
        val result = useCase.execute(players(12))

        assertEquals(4, result.matchSimulation.size)
        assertEquals(listOf(1, 2, 3, 4), result.matchSimulation.map { it.quarterNumber })
    }

    @Test
    fun execute_winProbabilitiesAreComplementaryAndInRange() {
        val result = useCase.execute(players(12))

        val (userAnalytics, aiAnalytics) = result.analytics
        assertEquals(1.0, userAnalytics.winProbability + aiAnalytics.winProbability, 1e-9)
        assertTrue(userAnalytics.winProbability in 0.0..1.0)
    }
}
