package com.g5

import com.g5.data.remote.dto.Auction
import com.g5.data.remote.dto.Match
import com.g5.data.remote.dto.MatchTeam
import com.g5.domain.model.NBAPlayer
import com.g5.ui.viewmodel.MatchUiState
import org.junit.Assert.*
import org.junit.Test

class MultiplayerAutoPassTest {

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

    private val myUserId = "user-123"
    private val opponentUserId = "user-456"

    @Test
    fun testCannotAffordNextBid_whenBudgetIsLessThanCurrentBidPlusOne() {
        val auction = Auction(
            id = "auction-1",
            matchId = "match-1",
            nbaPlayerId = 1,
            auctionType = "bid",
            currentBid = 10,
            currentBidderId = opponentUserId,
            turnUserId = myUserId,
            status = "active"
        )

        val myTeam = MatchTeam(
            id = "team-1",
            matchId = "match-1",
            userId = myUserId,
            budgetRemaining = 10 // Need at least 11 to bid
        )

        val state = MatchUiState(
            myUserId = myUserId,
            currentAuction = auction,
            myTeam = myTeam,
            isMyTurn = true
        )

        assertTrue(state.canPass)
        assertTrue(state.cannotAffordNextBid)
    }

    @Test
    fun testCannotAffordNextBid_whenBudgetIsSufficient_returnsFalse() {
        val auction = Auction(
            id = "auction-1",
            matchId = "match-1",
            nbaPlayerId = 1,
            auctionType = "bid",
            currentBid = 10,
            currentBidderId = opponentUserId,
            turnUserId = myUserId,
            status = "active"
        )

        val myTeam = MatchTeam(
            id = "team-1",
            matchId = "match-1",
            userId = myUserId,
            budgetRemaining = 15 // Sufficient to bid 11
        )

        val state = MatchUiState(
            myUserId = myUserId,
            currentAuction = auction,
            myTeam = myTeam,
            isMyTurn = true
        )

        assertTrue(state.canPass)
        assertFalse(state.cannotAffordNextBid)
    }

    @Test
    fun testCannotAffordNextBid_whenOpeningTurn_cannotPassYet() {
        val auction = Auction(
            id = "auction-1",
            matchId = "match-1",
            nbaPlayerId = 1,
            auctionType = "bid",
            currentBid = 0,
            currentBidderId = null, // Opening
            turnUserId = myUserId,
            status = "active"
        )

        val myTeam = MatchTeam(
            id = "team-1",
            matchId = "match-1",
            userId = myUserId,
            budgetRemaining = 0
        )

        val state = MatchUiState(
            myUserId = myUserId,
            currentAuction = auction,
            myTeam = myTeam,
            isMyTurn = true
        )

        assertFalse(state.canPass)
        assertFalse(state.cannotAffordNextBid)
    }

    @Test
    fun testCannotAffordNextBid_whenOpponentsTurn_returnsFalse() {
        val auction = Auction(
            id = "auction-1",
            matchId = "match-1",
            nbaPlayerId = 1,
            auctionType = "bid",
            currentBid = 10,
            currentBidderId = myUserId,
            turnUserId = opponentUserId,
            status = "active"
        )

        val myTeam = MatchTeam(
            id = "team-1",
            matchId = "match-1",
            userId = myUserId,
            budgetRemaining = 5
        )

        val state = MatchUiState(
            myUserId = myUserId,
            currentAuction = auction,
            myTeam = myTeam,
            isMyTurn = false
        )

        assertFalse(state.cannotAffordNextBid)
    }
}
