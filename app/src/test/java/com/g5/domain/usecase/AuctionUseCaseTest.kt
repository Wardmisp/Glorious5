package com.g5.domain.usecase

import com.g5.domain.model.GameState
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TeamEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuctionUseCaseTest {

    private lateinit var useCase: AuctionUseCase

    private fun player(id: Int = 1) = NBAPlayer(
        id = id,
        position = "Pivot",
        team = "GSW",
        season = "2023-24",
        pts = 20.0,
        reb = 10.0,
        ast = 5.0
    )

    @Before
    fun setUp() {
        useCase = AuctionUseCase()
    }

    // --- generateRevealOrder ---

    @Test
    fun generateRevealOrder_returnsAPermutationOfZeroToNine() {
        val order = useCase.generateRevealOrder()

        assertEquals(10, order.size)
        assertEquals((0..9).toSet(), order.toSet())
    }

    // --- startRound ---

    @Test
    fun startRound_resetsPerRoundFieldsButKeepsBudgetsTeamsAndPlayers() {
        val teamA = listOf(TeamEntry(player(1), 5))
        val teamB = listOf(TeamEntry(player(2), 7))
        val previous = GameState(
            round = 2,
            bid = 8,
            bidder = 1,
            p1Input = 9,
            p2Input = 9,
            bidCount = 3,
            timer = 0,
            budgets = 30 to 25,
            teams = teamA to teamB,
            players = listOf(player(1), player(2), player(3)),
            isVsHuman = true
        )

        val next = useCase.startRound(previous, roundIndex = 3)

        assertEquals(3, next.round)
        assertEquals(0, next.bid)
        assertNull(next.bidder)
        assertEquals(1, next.p1Input)
        assertEquals(1, next.p2Input)
        assertEquals(0, next.bidCount)
        assertEquals(15, next.timer)
        assertEquals(30 to 25, next.budgets)
        assertEquals(teamA to teamB, next.teams)
        assertEquals(previous.players, next.players)
        assertTrue(next.isVsHuman)
        assertEquals(10, next.revealOrder.size)
    }

    @Test
    fun startRound_alternatesStarterByRoundParity() {
        val previous = GameState()

        assertEquals(1, useCase.startRound(previous, roundIndex = 0).activePlayerTurn)
        assertEquals(2, useCase.startRound(previous, roundIndex = 1).activePlayerTurn)
        assertEquals(1, useCase.startRound(previous, roundIndex = 2).activePlayerTurn)
    }

    // --- isTeamFull ---

    @Test
    fun isTeamFull_trueOnceRosterReachesFiveOfTen() {
        val fiveEntries = (1..5).map { TeamEntry(player(it), 1) }
        val state = GameState(teams = fiveEntries to emptyList())

        assertTrue(useCase.isTeamFull(state, player = 1))
        assertFalse(useCase.isTeamFull(state, player = 2))
    }

    // --- resolvePass ---

    @Test
    fun resolvePass_recipientNotFull_currentBidderIsRecipient_keepsTheirBid() {
        val state = GameState(bid = 12, bidder = 2, teams = emptyList<TeamEntry>() to emptyList())

        val outcome = useCase.resolvePass(state, passedBy = 1)

        assertEquals(PassOutcome(awardedTo = 2, finalBid = 12), outcome)
    }

    @Test
    fun resolvePass_recipientNotFull_passerNotFull_noBidder_awardsForAtLeastOneDollar() {
        val state = GameState(bid = 0, bidder = null)

        val outcome = useCase.resolvePass(state, passedBy = 2)

        assertEquals(PassOutcome(awardedTo = 1, finalBid = 1), outcome)
    }

    @Test
    fun resolvePass_recipientNotFull_passerFull_noBidder_awardsForFree() {
        val fullTeam = (1..5).map { TeamEntry(player(it), 1) }
        // passedBy = 1, and team 1 (the passer) is full.
        val state = GameState(bid = 0, bidder = null, teams = fullTeam to emptyList())

        val outcome = useCase.resolvePass(state, passedBy = 1)

        assertEquals(PassOutcome(awardedTo = 2, finalBid = 0), outcome)
    }

    @Test
    fun resolvePass_recipientFull_passerNotFull_currentBidderIsPasser_keepsTheirBid() {
        val fullTeam = (1..5).map { TeamEntry(player(it), 1) }
        // passedBy = 1 passes; recipient (2) is full, so 1 (the passer) recovers the player.
        val state = GameState(bid = 9, bidder = 1, teams = emptyList<TeamEntry>() to fullTeam)

        val outcome = useCase.resolvePass(state, passedBy = 1)

        assertEquals(PassOutcome(awardedTo = 1, finalBid = 9), outcome)
    }

    @Test
    fun resolvePass_recipientFull_passerNotFull_someoneElseWasBidding_awardsForFree() {
        val fullTeam = (1..5).map { TeamEntry(player(it), 1) }
        val state = GameState(bid = 9, bidder = 2, teams = emptyList<TeamEntry>() to fullTeam)

        val outcome = useCase.resolvePass(state, passedBy = 1)

        assertEquals(PassOutcome(awardedTo = 1, finalBid = 0), outcome)
    }

    @Test
    fun resolvePass_bothTeamsFull_returnsNull() {
        val fullTeam = (1..5).map { TeamEntry(player(it), 1) }
        val state = GameState(teams = fullTeam to fullTeam)

        assertNull(useCase.resolvePass(state, passedBy = 1))
    }

    // --- adjudicate ---

    @Test
    fun adjudicate_awardsPlayerToBidderOneAndDebitsTheirBudget() {
        val state = GameState(budgets = 50 to 50, teams = emptyList<TeamEntry>() to emptyList())
        val player = player(7)

        val result = useCase.adjudicate(state, player, bid = 20, bidder = 1)

        assertEquals(30 to 50, result.budgets)
        assertEquals(listOf(TeamEntry(player, 20)), result.teams.first)
        assertTrue(result.teams.second.isEmpty())
        assertEquals(1, result.awardedTo)
        assertEquals(20, result.bid)
        assertTrue(result.done)
        assertFalse(result.thinking)
    }

    @Test
    fun adjudicate_awardsPlayerToBidderTwoAndDebitsTheirBudget() {
        val state = GameState(budgets = 50 to 50)
        val player = player(7)

        val result = useCase.adjudicate(state, player, bid = 15, bidder = 2)

        assertEquals(50 to 35, result.budgets)
        assertEquals(listOf(TeamEntry(player, 15)), result.teams.second)
        assertEquals(2, result.awardedTo)
    }

    @Test
    fun adjudicate_costIsCappedByRemainingBudget_neverGoesNegative() {
        val state = GameState(budgets = 5 to 50)
        val player = player(7)

        val result = useCase.adjudicate(state, player, bid = 999, bidder = 1)

        assertEquals(0, result.budgets.first)
        assertEquals(5, result.teams.first.single().paid)
        assertEquals(5, result.bid)
    }
}
