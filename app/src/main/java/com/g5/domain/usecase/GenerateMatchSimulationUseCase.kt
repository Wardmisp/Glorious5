package com.g5.domain.usecase

import com.g5.domain.model.GameAction
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.QuarterSimulation

class GenerateMatchSimulationUseCase {

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
        val position = actor.position.lowercase()
        
        val commonActions = mutableListOf(
            "Incroyable contre de ${actor.lastName} qui repousse la tentative de dunk de ${opponent.lastName}.",
            "${actor.lastName} intercepte le ballon dans les mains de ${opponent.lastName} et s'en va finir seul en contre-attaque !",
            "Duel physique intense : ${actor.lastName} gagne son duel au rebond face à ${opponent.lastName}.",
            "${actor.lastName} provoque le passage en force de ${opponent.lastName}, quel engagement !",
            "Défense d'acier : ${actor.lastName} ne laisse aucun espace à ${opponent.lastName} et force la perte de balle.",
            "Lecture de jeu parfaite : ${actor.lastName} anticipe la passe de ${opponent.lastName} et lance la transition.",
            "Quel sang-froid ! ${actor.lastName} feinte le tir et oblige ${opponent.lastName} à sauter dans le vide avant de marquer."
        )

        val backcourtActions = listOf(
            "${actor.lastName} marque un 3 points spectaculaire sur la tête de ${opponent.lastName} !",
            "Passe aveugle chirurgicale de ${actor.lastName} qui laisse ${opponent.lastName} totalement immobile.",
            "${actor.lastName} plante un step-back longue distance malgré la défense de ${opponent.lastName} !",
            "Touché de velours : ${actor.lastName} termine avec un floater élégant au-dessus de ${opponent.lastName}.",
            "${actor.lastName} enchaîne les dribbles et fait mordre la poussière à ${opponent.lastName} sur un cassage de chevilles !",
            "Séquence de haute volée : ${actor.lastName} efface ${opponent.lastName} d'un crossover dévastateur."
        )

        val frontcourtActions = listOf(
            "${actor.lastName} postérise violemment ${opponent.lastName} avec un dunk dévastateur !",
            "${actor.lastName} s'envole pour un alley-oop monumental, ${opponent.lastName} ne peut que regarder.",
            "Contre illégal ? Non ! ${actor.lastName} scotche proprement le ballon contre la planche devant ${opponent.lastName}.",
            "${actor.lastName} finit en force au cercle avec un 'and-one' spectaculaire face à ${opponent.lastName}.",
            "${actor.lastName} domine la raquette et arrache un rebond offensif crucial devant ${opponent.lastName}.",
            "Puissance pure : ${actor.lastName} enfonce ${opponent.lastName} au poste bas et finit avec un move d'école."
        )

        val wingActions = listOf(
            "${actor.lastName} traverse tout le terrain et finit par un lay-up malgré la faute de ${opponent.lastName}.",
            "Magnifique passe décisive de ${actor.lastName} alors que ${opponent.lastName} était en retard sur la rotation.",
            "${actor.lastName} déclenche un tir à mi-distance soyeux au-dessus des bras de ${opponent.lastName}."
        )

        val finalTemplates = commonActions.apply {
            when {
                position.contains("meneur") || position.contains("arrière") -> addAll(backcourtActions + wingActions)
                position.contains("pivot") || position.contains("intérieur") -> addAll(frontcourtActions)
                position.contains("fort") -> addAll(frontcourtActions + wingActions)
                else -> addAll(backcourtActions + frontcourtActions + wingActions)
            }
        }
        
        return GameAction(
            description = finalTemplates.random(),
            highlights = listOf(actor, opponent),
            favorsTeamA = favorsTeamA
        )
    }
}
