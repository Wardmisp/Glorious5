package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import com.g5.domain.provider.CommentaryKey
import com.g5.domain.provider.StringProvider
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

    /** N'a pas besoin de vraies ressources Android : renvoie juste un texte qui identifie sans
     * ambiguïté la clé et les deux noms passés, pour que les tests puissent vérifier le contenu. */
    private class FakeStringProvider : StringProvider {
        override fun commentary(key: CommentaryKey, actorName: String, opponentName: String): String =
            "$key|$actorName|$opponentName"
    }

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
        useCase = GenerateMatchSimulationUseCase(FakeStringProvider())
    }

    @Test
    fun execute_alwaysReturnsExactlyFourQuartersInOrder() {
        val teamA = listOf(player(1, "A", "PG"))
        val teamB = listOf(player(2, "B", "C"))

        val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

        assertEquals(4, simulation.size)
        assertEquals(listOf(1, 2, 3, 4), simulation.map { it.quarterNumber })
    }

    @Test
    fun execute_emptyTeam_producesQuartersWithNoActionsInsteadOfCrashing() {
        val simulation = useCase.execute(teamA = emptyList(), teamB = listOf(player(1, "B", "C")), winProbA = 0.5)

        assertEquals(4, simulation.size)
        assertTrue(simulation.all { it.actions.isEmpty() })
    }

    @Test
    fun execute_nonEmptyTeams_generatesExactlyThreeActionsPerQuarter() {
        val teamA = listOf(player(1, "A", "PG"))
        val teamB = listOf(player(2, "B", "C"))

        val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

        assertTrue(simulation.all { it.actions.size == 3 })
    }

    @Test
    fun execute_actionHighlightsAlwaysPairOneActorFromEachTeam() {
        // Avec une seule recrue par équipe, l'acteur et l'adverse ne peuvent être que ces deux-là,
        // quel que soit le tirage de isTeamAActing.
        val playerA = player(1, "A", "PG")
        val playerB = player(2, "B", "C")

        val simulation = useCase.execute(listOf(playerA), listOf(playerB), winProbA = 0.5)

        simulation.flatMap { it.actions }.forEach { action ->
            assertEquals(setOf(playerA, playerB), action.highlights.toSet())
        }
    }

    @Test
    fun execute_favorsTeamAFlagMatchesWhichTeamTheActorBelongsTo() {
        val playerA = player(1, "A", "PG")
        val playerB = player(2, "B", "C")

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
        val teamA = listOf(player(1, "A", "PG"))
        val teamB = listOf(player(2, "B", "C"))

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
        val positions = listOf("PG", "SG", "SF", "PF", "C", "FC", "GF", "") // "" = poste inconnu

        positions.forEach { position ->
            val teamA = listOf(player(1, "A", position))
            val teamB = listOf(player(2, "B", "SF"))

            val simulation = useCase.execute(teamA, teamB, winProbA = 0.5)

            simulation.flatMap { it.actions }.forEach { action ->
                assertTrue(action.description.isNotBlank())
            }
        }
    }

    @Test
    fun execute_pointGuard_onlyDrawsFromCommonAndBackcourtCommentary() {
        val teamA = listOf(player(1, "Actor", "PG"))
        val teamB = listOf(player(2, "Opponent", "C"))
        val allowedKeys = (CommentaryKey.COMMON + CommentaryKey.BACKCOURT).map { it.toString() }.toSet()

        val simulation = useCase.execute(teamA, teamB, winProbA = 1.0) // team A agit à tous les coups

        simulation.flatMap { it.actions }.forEach { action ->
            val key = action.description.substringBefore('|')
            assertTrue("unexpected key for PG: $key", key in allowedKeys)
        }
    }

    @Test
    fun execute_center_onlyDrawsFromCommonAndFrontcourtCommentary() {
        val teamA = listOf(player(1, "Actor", "C"))
        val teamB = listOf(player(2, "Opponent", "PG"))
        val allowedKeys = (CommentaryKey.COMMON + CommentaryKey.FRONTCOURT).map { it.toString() }.toSet()

        val simulation = useCase.execute(teamA, teamB, winProbA = 1.0)

        simulation.flatMap { it.actions }.forEach { action ->
            val key = action.description.substringBefore('|')
            assertTrue("unexpected key for C: $key", key in allowedKeys)
        }
    }
}
