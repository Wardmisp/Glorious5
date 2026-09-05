package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * generateActionsForQuarter/generateRandomAction sont privées et truffées de tirages aléatoires
 * (Math.random(), .random(), .shuffled()) sans point d'injection : on vérifie donc des invariants
 * structurels tenables quel que soit le tirage, plutôt que des valeurs exactes. L'astuce pour
 * rendre les acteurs déterministes malgré le hasard : des équipes à un seul joueur, puisque
 * `List(1).random()` ne peut renvoyer qu'un seul élément possible.
 */
class GenerateMatchSimulationUseCaseTest {

    private lateinit var useCase: GenerateMatchSimulationUseCase

    private fun player(id: Int, lastName: String, position: String) = NBAPlayer(
        id = id,
        lastName = lastName,
        position = position,
        team = "TEAM",
        season = "2023-24",
        pts = 10.0,
        reb = 5.0,
        ast = 5.0
    )

    @Before
    fun setUp() {
        useCase = GenerateMatchSimulationUseCase()
    }

    @Test
    fun execute_alwaysReturnsExactlyFourQuartersInOrder() {
        val teamA = listOf(player(1, "A", "Meneur"))
        val teamB = listOf(player(2, "B", "Pivot"))

        val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

        assertEquals(4, simulation.size)
        assertEquals(listOf(1, 2, 3, 4), simulation.map { it.quarterNumber })
    }

    @Test
    fun execute_emptyTeam_producesQuartersWithNoActionsInsteadOfCrashing() {
        val simulation = useCase.execute(teamA = emptyList(), teamB = listOf(player(1, "B", "Pivot")), winProbA = 0.5)

        assertEquals(4, simulation.size)
        assertTrue(simulation.all { it.actions.isEmpty() })
    }

    @Test
    fun execute_nonEmptyTeams_generatesExactlyThreeActionsPerQuarter() {
        val teamA = listOf(player(1, "A", "Meneur"))
        val teamB = listOf(player(2, "B", "Pivot"))

        val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

        assertTrue(simulation.all { it.actions.size == 3 })
    }

    @Test
    fun execute_actionHighlightsAlwaysPairOneActorFromEachTeam() {
        // Avec une seule recrue par équipe, l'acteur et l'adverse ne peuvent être que ces deux-là,
        // quel que soit le tirage de isTeamAActing.
        val playerA = player(1, "A", "Meneur")
        val playerB = player(2, "B", "Pivot")

        val simulation = useCase.execute(listOf(playerA), listOf(playerB), winProbA = 0.5)

        simulation.flatMap { it.actions }.forEach { action ->
            assertEquals(setOf(playerA, playerB), action.highlights.toSet())
        }
    }

    @Test
    fun execute_favorsTeamAFlagMatchesWhichTeamTheActorBelongsTo() {
        val playerA = player(1, "A", "Meneur")
        val playerB = player(2, "B", "Pivot")

        // De nombreuses répétitions pour couvrir les deux branches du tirage isTeamAActing.
        repeat(100) {
            val simulation = useCase.execute(listOf(playerA), listOf(playerB), winProbA = 0.5)
            simulation.flatMap { it.actions }.forEach { action ->
                val (actor, opponent) = action.highlights[0] to action.highlights[1]
                if (action.favorsTeamA) {
                    assertEquals(playerA, actor)
                    assertEquals(playerB, opponent)
                } else {
                    assertEquals(playerB, actor)
                    assertEquals(playerA, opponent)
                }
            }
        }
    }

    @Test
    fun execute_actionTimesAreDistinctInRangeAndSortedDescendingWithinAQuarter() {
        val teamA = listOf(player(1, "A", "Meneur"))
        val teamB = listOf(player(2, "B", "Pivot"))

        val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

        simulation.forEach { quarter ->
            val times = quarter.actions.map { it.timeSeconds }
            assertEquals(times.size, times.toSet().size) // pas de doublon
            assertTrue(times.all { it in 1..719 })
            assertEquals(times.sortedDescending(), times)
        }
    }

    @Test
    fun execute_descriptionIsNeverBlank_forEveryKnownPosition() {
        val positions = listOf("Meneur", "Arrière", "Ailier", "Ailier Fort", "Pivot", "Intérieur", "Polyvalent")

        positions.forEach { position ->
            val teamA = listOf(player(1, "A", position))
            val teamB = listOf(player(2, "B", "Ailier"))

            val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

            simulation.flatMap { it.actions }.forEach { action ->
                assertTrue(action.description.isNotBlank())
            }
        }
    }
}
