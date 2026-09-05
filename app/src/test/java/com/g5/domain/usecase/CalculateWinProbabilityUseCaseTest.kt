package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.exp

/**
 * getPercentile/calculatePlayerScore/calculateTeamScore sont privÃ©es : on les vÃ©rifie en boÃ®te
 * noire via execute(), en choisissant des jeux de donnÃ©es dont le pourcentile de chaque
 * statistique est calculable Ã  la main plutÃ´t qu'en se contentant de bornes.
 */
class CalculateWinProbabilityUseCaseTest {

    private lateinit var useCase: CalculateWinProbabilityUseCase

    private fun player(
        id: Int,
        position: String,
        pts: Double,
        reb: Double,
        ast: Double,
        stl: Double,
        blk: Double,
        fgPct: Double = 0.0,
        fg3Pct: Double = 0.0,
        ftPct: Double = 0.0,
        per: Double = 0.0,
        winShares: Double = 0.0,
        games: Int = 0
    ) = NBAPlayer(
        id = id,
        position = position,
        team = "TEAM",
        season = "2023-24",
        pts = pts,
        reb = reb,
        ast = ast,
        stl = stl,
        blk = blk,
        fgPct = fgPct,
        fg3Pct = fg3Pct,
        ftPct = ftPct,
        per = per,
        winShares = winShares,
        games = games
    )

    @Before
    fun setUp() {
        useCase = CalculateWinProbabilityUseCase()
    }

    @Test
    fun execute_playerStrictlyAheadOnEveryStat_scoresExactly75thPercentileOnEveryComponent() {
        // Distribution Ã  2 joueurs : quand toutes les stats de X dominent strictement celles de Y,
        // chaque pourcentile de X vaut (countBelow=1 + 0.5*countEqual=1) / 2 * 100 = 75, et celui
        // de Y vaut (0 + 0.5*1) / 2 * 100 = 25 â€” pour CHAQUE statistique, donc aussi pour EFF,
        // IMPACT et le score total (dont les poids somment Ã  1.0).
        val strong = player(
            id = 1, position = "PG",
            pts = 30.0, reb = 12.0, ast = 8.0, stl = 3.0, blk = 2.0,
            fgPct = 0.55, fg3Pct = 0.40, ftPct = 0.85, per = 28.0, winShares = 12.0, games = 70
        )
        val weak = player(
            id = 2, position = "C",
            pts = 10.0, reb = 4.0, ast = 2.0, stl = 0.5, blk = 0.2,
            fgPct = 0.40, fg3Pct = 0.30, ftPct = 0.70, per = 12.0, winShares = 4.0, games = 70
        )
        val allSeasons = listOf(strong, weak)

        val (analyticsA, analyticsB) = useCase.execute(teamA = listOf(strong), teamB = listOf(weak), allSeasons = allSeasons)

        assertEquals(75.0, analyticsA.scoredPlayers.single().totalScore, 1e-9)
        assertEquals(25.0, analyticsB.scoredPlayers.single().totalScore, 1e-9)
    }

    @Test
    fun execute_teamScoreIsTotalScoreWeightedByPositionalImportance() {
        val meneur = player(
            id = 1, position = "PG",
            pts = 30.0, reb = 12.0, ast = 8.0, stl = 3.0, blk = 2.0,
            fgPct = 0.55, fg3Pct = 0.40, ftPct = 0.85, per = 28.0, winShares = 12.0, games = 70
        )
        val pivot = player(
            id = 2, position = "C",
            pts = 10.0, reb = 4.0, ast = 2.0, stl = 0.5, blk = 0.2,
            fgPct = 0.40, fg3Pct = 0.30, ftPct = 0.70, per = 12.0, winShares = 4.0, games = 70
        )
        val allSeasons = listOf(meneur, pivot)

        val (analyticsA, analyticsB) = useCase.execute(teamA = listOf(meneur), teamB = listOf(pivot), allSeasons = allSeasons)

        // totalScore = 75 pour "meneur" (poids 0.22) et 25 pour "pivot" (poids 0.18) â€” meneur/pivot
        // dominent (ou sont dominÃ©s) sur CHAQUE statistique, cf. le test prÃ©cÃ©dent.
        assertEquals(75.0 * 0.22, analyticsA.teamScore, 1e-9)
        assertEquals(25.0 * 0.18, analyticsB.teamScore, 1e-9)
    }

    @Test
    fun execute_unknownPosition_fallsBackToDefaultWeightOfTwentyPercent() {
        // Distribution Ã  un seul joueur, lui-mÃªme : chaque pourcentile vaut (0 + 0.5*1)/1*100 = 50,
        // donc totalScore = 50.0 exactement (les poids des composantes somment Ã  1.0).
        val soloPlayer = player(id = 1, position = "XX", pts = 15.0, reb = 6.0, ast = 4.0, stl = 1.0, blk = 1.0)

        val (analyticsA, _) = useCase.execute(teamA = listOf(soloPlayer), teamB = emptyList(), allSeasons = listOf(soloPlayer))

        assertEquals(50.0, analyticsA.scoredPlayers.single().totalScore, 1e-9)
        assertEquals(50.0 * 0.20, analyticsA.teamScore, 1e-9)
    }

    @Test
    fun execute_zeroGamesPlayed_treatsWinSharesPerGameAsZeroInsteadOfCrashing() {
        // games = 0 doit court-circuiter la division par zÃ©ro (wsPerGame = 0.0) plutÃ´t que planter,
        // et rester traitable comme n'importe quelle autre statistique par getPercentile.
        val neverPlayed = player(id = 1, position = "SF", pts = 5.0, reb = 2.0, ast = 1.0, stl = 0.1, blk = 0.1, winShares = 9.0, games = 0)

        val (analyticsA, _) = useCase.execute(teamA = listOf(neverPlayed), teamB = emptyList(), allSeasons = listOf(neverPlayed))

        // Distribution Ã  un seul joueur -> 50e percentile partout, y compris pour WS/Game.
        assertEquals(50.0, analyticsA.scoredPlayers.single().wsPercentile, 1e-9)
        assertEquals(50.0, analyticsA.scoredPlayers.single().totalScore, 1e-9)
    }

    @Test
    fun execute_strongerTeamGetsTheLogisticWinProbabilityOfTheScoreDifference() {
        val strong = player(
            id = 1, position = "PG",
            pts = 30.0, reb = 12.0, ast = 8.0, stl = 3.0, blk = 2.0,
            fgPct = 0.55, fg3Pct = 0.40, ftPct = 0.85, per = 28.0, winShares = 12.0, games = 70
        )
        val weak = player(
            id = 2, position = "C",
            pts = 10.0, reb = 4.0, ast = 2.0, stl = 0.5, blk = 0.2,
            fgPct = 0.40, fg3Pct = 0.30, ftPct = 0.70, per = 12.0, winShares = 4.0, games = 70
        )
        val allSeasons = listOf(strong, weak)

        val (analyticsA, analyticsB) = useCase.execute(teamA = listOf(strong), teamB = listOf(weak), allSeasons = allSeasons)

        val expectedDiff = (75.0 * 0.22) - (25.0 * 0.18)
        val expectedProbA = 1.0 / (1.0 + exp(-expectedDiff / 8.0))

        assertEquals(expectedProbA, analyticsA.winProbability, 1e-9)
        assertTrue(analyticsA.winProbability > 0.5)
    }

    @Test
    fun execute_winProbabilitiesAreAlwaysComplementary() {
        val strong = player(id = 1, position = "PG", pts = 30.0, reb = 12.0, ast = 8.0, stl = 3.0, blk = 2.0)
        val weak = player(id = 2, position = "C", pts = 10.0, reb = 4.0, ast = 2.0, stl = 0.5, blk = 0.2)

        val (analyticsA, analyticsB) = useCase.execute(teamA = listOf(strong), teamB = listOf(weak), allSeasons = listOf(strong, weak))

        assertEquals(1.0, analyticsA.winProbability + analyticsB.winProbability, 1e-9)
    }

    @Test
    fun execute_identicalTeams_giveEvenOdds() {
        val player = player(id = 1, position = "SF", pts = 20.0, reb = 5.0, ast = 5.0, stl = 1.0, blk = 1.0)

        val (analyticsA, analyticsB) = useCase.execute(teamA = listOf(player), teamB = listOf(player), allSeasons = listOf(player))

        assertEquals(0.5, analyticsA.winProbability, 1e-9)
        assertEquals(0.5, analyticsB.winProbability, 1e-9)
        assertEquals(analyticsA.teamScore, analyticsB.teamScore, 1e-9)
    }

    @Test
    fun execute_emptyTeams_scoreZeroAndSplitEvenly() {
        val (analyticsA, analyticsB) = useCase.execute(teamA = emptyList(), teamB = emptyList(), allSeasons = emptyList())

        assertEquals(0.0, analyticsA.teamScore, 1e-9)
        assertEquals(0.0, analyticsB.teamScore, 1e-9)
        assertEquals(0.5, analyticsA.winProbability, 1e-9)
        assertTrue(analyticsA.scoredPlayers.isEmpty())
    }
}
