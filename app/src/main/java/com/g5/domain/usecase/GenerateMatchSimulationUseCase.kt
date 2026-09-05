package com.g5.domain.usecase

import com.g5.domain.model.GameAction
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.QuarterSimulation
import com.g5.domain.provider.CommentaryKey
import com.g5.domain.provider.StringProvider

class GenerateMatchSimulationUseCase(private val stringProvider: StringProvider) {

    fun execute(teamA: List<NBAPlayer>, teamB: List<NBAPlayer>, winProbA: Double): List<QuarterSimulation> {
        return (1..4).map { q ->
            QuarterSimulation(
                quarterNumber = q,
                actions = generateActionsForQuarter(q, teamA, teamB, winProbA)
            )
        }
    }

    private fun generateActionsForQuarter(q: Int, teamA: List<NBAPlayer>, teamB: List<NBAPlayer>, winProbA: Double): List<GameAction> {
        val actions = mutableListOf<GameAction>()
        if (teamA.isEmpty() || teamB.isEmpty()) return actions

        // On génère 3 actions par quart-temps
        val actionCount = 3
        val times = (1 until 720).shuffled().take(actionCount).sortedDescending()

        repeat(actionCount) { i ->
            val isTeamAActing = Math.random() < winProbA
            val actor = if (isTeamAActing) teamA.random() else teamB.random()
            val opponent = if (isTeamAActing) teamB.random() else teamA.random()

            val action = generateRandomAction(actor, opponent, isTeamAActing)
            actions.add(action.copy(timeSeconds = times[i]))
        }

        return actions
    }

    private fun generateRandomAction(actor: NBAPlayer, opponent: NBAPlayer, favorsTeamA: Boolean): GameAction {
        // Poste regroupé par grande famille de jeu — voir normalizePositionCode() côté data pour
        // l'origine des codes (PG/SG/SF/PF/C/GF/FC).
        val candidates = CommentaryKey.COMMON + when (actor.position) {
            "PG", "SG" -> CommentaryKey.BACKCOURT
            "C", "FC" -> CommentaryKey.FRONTCOURT
            "PF" -> CommentaryKey.FRONTCOURT + CommentaryKey.WING
            else -> CommentaryKey.BACKCOURT + CommentaryKey.FRONTCOURT + CommentaryKey.WING
        }

        val description = stringProvider.commentary(candidates.random(), actor.lastName, opponent.lastName)

        return GameAction(
            description = description,
            highlights = listOf(actor, opponent),
            favorsTeamA = favorsTeamA
        )
    }
}
