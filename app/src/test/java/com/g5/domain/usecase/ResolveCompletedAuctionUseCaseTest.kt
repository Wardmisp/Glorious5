package com.g5.domain.usecase

import com.g5.data.remote.dto.Auction
import com.g5.domain.model.CompletedAuctionInfo
import com.g5.domain.model.NBAPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class ResolveCompletedAuctionUseCaseTest {

    private lateinit var useCase: ResolveCompletedAuctionUseCase

    private val myId = "user-me"
    private val opponentId = "user-opponent"

    private val winningPlayer = NBAPlayer(
        id = 42,
        position = "Meneur",
        team = "GSW",
        season = "2015-16",
        pts = 30.1,
        reb = 5.4,
        ast = 6.7
    )

    private fun completedAuction(
        id: String = "auction-1",
        winnerId: String? = myId,
        finalPrice: Int? = 12,
        auctionType: String = "bid",
        nbaPlayerId: Int = 42
    ) = Auction(
        id = id,
        matchId = "match-1",
        nbaPlayerId = nbaPlayerId,
        auctionType = auctionType,
        status = "completed",
        turnUserId = myId,
        winnerId = winnerId,
        finalPrice = finalPrice
    )

    @Before
    fun setUp() {
        useCase = ResolveCompletedAuctionUseCase()
    }

    @Test
    fun execute_returnsExistingPendingResultUnchanged_untilItIsDismissed() {
        val existing = CompletedAuctionInfo(
            auctionId = "auction-0",
            player = winningPlayer,
            winnerIsMe = true,
            pricePaid = 5,
            isAutoAssigned = false,
            isLastPick = false
        )

        val result = useCase.execute(
            latestAuction = completedAuction(id = "auction-1"), // a newer auction is already available
            previousPendingResult = existing,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertSame(existing, result)
    }

    @Test
    fun execute_noAuctionYet_returnsNull() {
        val result = useCase.execute(
            latestAuction = null,
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = emptyMap(),
            myRosterSize = 0,
            opponentRosterSize = 0,
            teamSize = 5
        )

        assertNull(result)
    }

    @Test
    fun execute_auctionStillActive_returnsNull() {
        val active = completedAuction().copy(status = "active")

        val result = useCase.execute(
            latestAuction = active,
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertNull(result)
    }

    @Test
    fun execute_alreadyDismissedAuction_returnsNull() {
        val result = useCase.execute(
            latestAuction = completedAuction(id = "auction-1"),
            previousPendingResult = null,
            lastDismissedAuctionId = "auction-1",
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertNull(result)
    }

    @Test
    fun execute_playerMissingFromHydratedPool_returnsNull() {
        val result = useCase.execute(
            latestAuction = completedAuction(),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = emptyMap(), // le joueur référencé par l'enchère n'a pas été chargé
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertNull(result)
    }

    @Test
    fun execute_winnerIdMissing_returnsNull() {
        val result = useCase.execute(
            latestAuction = completedAuction(winnerId = null),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertNull(result)
    }

    @Test
    fun execute_iWonTheAuction_buildsInfoWithWinnerIsMeTrue() {
        val result = useCase.execute(
            latestAuction = completedAuction(winnerId = myId, finalPrice = 17),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 2,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertEquals(
            CompletedAuctionInfo(
                auctionId = "auction-1",
                player = winningPlayer,
                winnerIsMe = true,
                pricePaid = 17,
                isAutoAssigned = false,
                isLastPick = false
            ),
            result
        )
    }

    @Test
    fun execute_opponentWonTheAuction_buildsInfoWithWinnerIsMeFalse() {
        val result = useCase.execute(
            latestAuction = completedAuction(winnerId = opponentId),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertEquals(false, result?.winnerIsMe)
    }

    @Test
    fun execute_missingFinalPrice_defaultsToZero() {
        val result = useCase.execute(
            latestAuction = completedAuction(finalPrice = null),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertEquals(0, result?.pricePaid)
    }

    @Test
    fun execute_autoAssignedAuction_flagsIsAutoAssigned() {
        val result = useCase.execute(
            latestAuction = completedAuction(auctionType = "auto_assign"),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 1,
            opponentRosterSize = 1,
            teamSize = 5
        )

        assertEquals(true, result?.isAutoAssigned)
    }

    @Test
    fun execute_bothRostersReachedTeamSize_flagsIsLastPick() {
        val result = useCase.execute(
            latestAuction = completedAuction(),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 5,
            opponentRosterSize = 5,
            teamSize = 5
        )

        assertEquals(true, result?.isLastPick)
    }

    @Test
    fun execute_oneRosterStillIncomplete_isNotTheLastPick() {
        val result = useCase.execute(
            latestAuction = completedAuction(),
            previousPendingResult = null,
            lastDismissedAuctionId = null,
            myId = myId,
            players = mapOf(42 to winningPlayer),
            myRosterSize = 5,
            opponentRosterSize = 4,
            teamSize = 5
        )

        assertEquals(false, result?.isLastPick)
    }
}
