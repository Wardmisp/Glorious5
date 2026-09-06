package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.QuarterSimulation
import com.g5.domain.model.TeamAnalytics
import com.g5.domain.model.TeamEntry

/** Construit la démo (équipes + analytics + simulation) montrée pendant le tutoriel — des données
 * factices mais réalistes, indépendantes de toute vraie partie. */
class BuildTutorialDemoUseCase(
    private val calculateWinProbabilityUseCase: CalculateWinProbabilityUseCase,
    private val generateMatchSimulationUseCase: GenerateMatchSimulationUseCase
) {
    data class Result(
        val teams: Pair<List<TeamEntry>, List<TeamEntry>>,
        val analytics: Pair<TeamAnalytics, TeamAnalytics>,
        val matchSimulation: List<QuarterSimulation>
    )

    fun execute(allPlayers: List<NBAPlayer>): Result {
        // L'IA reçoit le gratin (les 5 meilleurs).
        val aiPlayers = allPlayers.take(5)

        // L'utilisateur reçoit des joueurs nettement moins forts (le bas du classement) : dans le
        // top 300, les derniers sont d'excellents joueurs mais bien moins "historiques".
        val userPlayers = if (allPlayers.size > 10) allPlayers.takeLast(5) else allPlayers.drop(5).take(5)

        val analytics = calculateWinProbabilityUseCase.execute(
            teamA = userPlayers,
            teamB = aiPlayers,
            allSeasons = allPlayers
        )
        val simulation = generateMatchSimulationUseCase.execute(
            teamA = userPlayers,
            teamB = aiPlayers,
            winProbA = analytics.first.winProbability
        )

        return Result(
            teams = userPlayers.map { TeamEntry(it, 5) } to aiPlayers.map { TeamEntry(it, 45) },
            analytics = analytics,
            matchSimulation = simulation
        )
    }
}
