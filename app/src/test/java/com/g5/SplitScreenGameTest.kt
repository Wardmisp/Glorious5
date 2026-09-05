package com.g5

import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TeamEntry
import com.g5.domain.model.GameState
import org.junit.Assert.*
import org.junit.Test

class SplitScreenGameTest {

    private fun createTestPlayer(id: Int, name: String, cost: Int = 0): NBAPlayer {
        return NBAPlayer(
            id = id,
            firstName = name,
            lastName = "Player",
            position = "Meneur",
            team = "LAL",
            teamColor = "#552583",
            season = "2023-24",
            pts = 20.0,
            reb = 5.0,
            ast = 5.0,
            stl = 1.0,
            blk = 0.5,
            fgPct = 0.5,
            fg3Pct = 0.35,
            ftPct = 0.8,
            per = 20.0,
            winShares = 5.0,
            games = 70
        )
    }

    @Test
    fun testInitialGameStateForVsHuman() {
        val state = GameState(
            budgets = Pair(50, 50),
            teams = Pair(emptyList(), emptyList()),
            activePlayerTurn = 1,
            isVsHuman = true
        )

        assertTrue(state.isVsHuman)
        assertEquals(1, state.activePlayerTurn)
        assertEquals(50, state.budgets.first)
        assertEquals(50, state.budgets.second)
        assertEquals(0, state.round)
        assertEquals(0, state.bid)
        assertNull(state.bidder)
        assertFalse(state.done)
    }

    @Test
    fun testTurnAlternationOnBids() {
        var state = GameState(
            budgets = Pair(50, 50),
            activePlayerTurn = 1,
            isVsHuman = true
        )

        // Joueur 1 mise $5
        state = state.copy(
            bid = 5,
            bidder = 1,
            bidCount = state.bidCount + 1,
            activePlayerTurn = 2,
            p2Input = 6
        )

        assertEquals(5, state.bid)
        assertEquals(1, state.bidder)
        assertEquals(2, state.activePlayerTurn)
        assertEquals(6, state.p2Input)

        // Joueur 2 surenchérit à $8
        state = state.copy(
            bid = 8,
            bidder = 2,
            bidCount = state.bidCount + 1,
            activePlayerTurn = 1,
            p1Input = 9
        )

        assertEquals(8, state.bid)
        assertEquals(2, state.bidder)
        assertEquals(1, state.activePlayerTurn)
        assertEquals(9, state.p1Input)
    }

    @Test
    fun testPassAwardingLogic() {
        val player = createTestPlayer(1, "Test")
        var state = GameState(
            players = listOf(player),
            budgets = Pair(50, 50),
            bid = 7,
            bidder = 1,
            activePlayerTurn = 2,
            isVsHuman = true
        )

        // Joueur 2 passe -> Joueur 1 remporte pour $7
        val winner = 1
        val cost = state.bid
        state = state.copy(
            budgets = Pair(state.budgets.first - cost, state.budgets.second),
            teams = Pair(listOf(TeamEntry(player, cost)), emptyList()),
            awardedTo = winner,
            done = true
        )

        assertTrue(state.done)
        assertEquals(1, state.awardedTo)
        assertEquals(43, state.budgets.first)
        assertEquals(50, state.budgets.second)
        assertEquals(1, state.teams.first.size)
        assertEquals(0, state.teams.second.size)
    }
}
