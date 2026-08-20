package com.g5.domain.usecase

import com.g5.domain.model.GameAction
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.QuarterSimulation

class GenerateMatchSimulationUseCase {

    fun execute(teamA: List<NBAPlayer>, teamB: List<NBAPlayer>): List<QuarterSimulation> {
        return (1..4).map { q ->
            QuarterSimulation(
                quarterNumber = q,
                actions = generateActionsForQuarter(q, teamA, teamB)
            )
        }
    }

    private fun generateActionsForQuarter(q: Int, teamA: List<NBAPlayer>, teamB: List<NBAPlayer>): List<GameAction> {
        val actions = mutableListOf<GameAction>()
        if (teamA.isEmpty() || teamB.isEmpty()) return actions
        
        // On génère 2 actions par quart-temps
        repeat(2) {
            val isTeamAActing = (0..1).random() == 0
            val actor = if (isTeamAActing) teamA.random() else teamB.random()
            val opponent = if (isTeamAActing) teamB.random() else teamA.random()
            
            actions.add(generateRandomAction(actor, opponent))
        }
        
        return actions
    }

    private fun generateRandomAction(actor: NBAPlayer, opponent: NBAPlayer): GameAction {
        val templates = listOf(
            "${actor.lastName} marque un 3 points spectaculaire sur la tête de ${opponent.lastName} !",
            "Incroyable contre de ${actor.lastName} qui repousse la tentative de dunk de ${opponent.lastName}.",
            "${actor.lastName} traverse tout le terrain et finit par un lay-up malgré la faute de ${opponent.lastName}.",
            "Magnifique passe décisive de ${actor.lastName} alors que ${opponent.lastName} était en retard sur la rotation.",
            "${actor.lastName} intercepte le ballon dans les mains de ${opponent.lastName} et s'en va dunker seul en contre-attaque !",
            "Duel physique intense : ${actor.lastName} gagne son duel au rebond face à ${opponent.lastName}.",
            "${actor.lastName} provoque le passage en force de ${opponent.lastName}, quel engagement !",
            "Séquence de haute volée : ${actor.lastName} efface ${opponent.lastName} d'un crossover dévastateur."
        )
        
        return GameAction(
            description = templates.random(),
            highlights = listOf(actor, opponent)
        )
    }
}
