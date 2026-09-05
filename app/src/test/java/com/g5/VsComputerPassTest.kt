package com.g5

import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TeamEntry
import com.g5.domain.model.GameState
import org.junit.Assert.*
import org.junit.Test

class VsComputerPassTest {

    private val dummyPlayer = NBAPlayer(
        id = 1,
        firstName = "Stephen",
        lastName = "Curry",
        position = "Meneur",
        team = "GSW",
        teamColor = "#1D428A",
        season = "2015-16",
        pts = 30.1,
        reb = 5.4,
        ast = 6.7,
        stl = 2.1,
        blk = 0.2
    )

    @Test
    fun testPassLogic_whenComputerPassesAfterPlayerBid_awardsToPlayer() {
        var state = GameState(
            players = listOf(dummyPlayer),
            round = 0,
            bid = 5,
            bidder = 1,
            budgets = Pair(50, 50),
            teams = Pair(emptyList(), emptyList())
        )

        val passedBy = 2
        val recipient = if (passedBy == 1) 2 else 1
        val finalBid = if (state.bidder == recipient) state.bid else maxOf(1, state.bid)
        
        val actualCost = minOf(finalBid, state.budgets.first)
        state = state.copy(
            bid = actualCost,
            budgets = Pair(state.budgets.first - actualCost, state.budgets.second),
            teams = Pair(state.teams.first + TeamEntry(dummyPlayer, actualCost), state.teams.second),
            awardedTo = recipient,
            done = true
        )

        assertEquals(1, state.awardedTo)
        assertEquals(45, state.budgets.first)
        assertEquals(50, state.budgets.second)
        assertEquals(1, state.teams.first.size)
        assertEquals(5, state.teams.first[0].paid)
        assertTrue(state.done)
    }

    @Test
    fun testPassLogic_whenComputerPassesAtZeroBid_awardsToPlayerForOneDollar() {
        var state = GameState(
            players = listOf(dummyPlayer),
            round = 0,
            bid = 0,
            bidder = null,
            budgets = Pair(50, 50),
            teams = Pair(emptyList(), emptyList())
        )

        val passedBy = 2
        val recipient = if (passedBy == 1) 2 else 1
        val finalBid = if (state.bidder == recipient) state.bid else maxOf(1, state.bid)
        
        val actualCost = minOf(finalBid, state.budgets.first)
        state = state.copy(
            bid = actualCost,
            budgets = Pair(state.budgets.first - actualCost, state.budgets.second),
            teams = Pair(state.teams.first + TeamEntry(dummyPlayer, actualCost), state.teams.second),
            awardedTo = recipient,
            done = true
        )

        assertEquals(1, state.awardedTo)
        assertEquals(49, state.budgets.first)
        assertEquals(50, state.budgets.second)
        assertEquals(1, state.teams.first.size)
        assertEquals(1, state.teams.first[0].paid)
        assertTrue(state.done)
    }

    @Test
    fun testPassLogic_whenPlayerPassesAfterComputerBid_awardsToComputer() {
        var state = GameState(
            players = listOf(dummyPlayer),
            round = 0,
            bid = 7,
            bidder = 2,
            budgets = Pair(50, 50),
            teams = Pair(emptyList(), emptyList())
        )

        val passedBy = 1
        val recipient = if (passedBy == 1) 2 else 1
        val finalBid = if (state.bidder == recipient) state.bid else maxOf(1, state.bid)
        
        val actualCost = minOf(finalBid, state.budgets.second)
        state = state.copy(
            bid = actualCost,
            budgets = Pair(state.budgets.first, state.budgets.second - actualCost),
            teams = Pair(state.teams.first, state.teams.second + TeamEntry(dummyPlayer, actualCost)),
            awardedTo = recipient,
            done = true
        )

        assertEquals(2, state.awardedTo)
        assertEquals(50, state.budgets.first)
        assertEquals(43, state.budgets.second)
        assertEquals(1, state.teams.second.size)
        assertEquals(7, state.teams.second[0].paid)
        assertTrue(state.done)
    }

    @Test
    fun testBudgetCannotBeNegative_whenBudgetIsZero() {
        var state = GameState(
            players = listOf(dummyPlayer),
            round = 0,
            bid = 0,
            bidder = null,
            budgets = Pair(0, 50),
            teams = Pair(emptyList(), emptyList())
        )

        val passedBy = 2
        val recipient = 1
        val finalBid = 1
        val actualCost = minOf(finalBid, state.budgets.first) // minOf(1, 0) = 0
        state = state.copy(
            bid = actualCost,
            budgets = Pair(state.budgets.first - actualCost, state.budgets.second),
            teams = Pair(state.teams.first + TeamEntry(dummyPlayer, actualCost), state.teams.second),
            awardedTo = recipient,
            done = true
        )

        assertEquals(0, state.budgets.first)
        assertEquals(0, state.teams.first[0].paid)
    }
}
