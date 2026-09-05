package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ComputerBidUseCaseTest {

    private lateinit var useCase: ComputerBidUseCase

    private fun player(pts: Double, reb: Double, ast: Double, stl: Double = 0.0, blk: Double = 0.0) = NBAPlayer(
        id = 1,
        position = "Pivot",
        team = "GSW",
        season = "2023-24",
        pts = pts,
        reb = reb,
        ast = ast,
        stl = stl,
        blk = blk
    )

    @Before
    fun setUp() {
        useCase = ComputerBidUseCase()
    }

    // --- evaluate ---
    // La composante "aléatoire" (randomFactor) est en réalité déterministe : elle dérive de
    // seed.hashCode() où seed = round + aiBudget/10, sans appel à Random. Ça permet de calculer
    // la valorisation attendue à la main plutôt que de se contenter de bornes.

    @Test
    fun evaluate_computesExactValuationForAModerateHand() {
        // seed = 0 + 50/10 = 5 -> randomFactor = 0.8 + (5 % 40)/100.0 = 0.85
        // baseValuation = 20*0.8 + 10*0.5 + 5*0.6 + 2*1.5 + 1*1.5 = 28.5
        // personalValuation = (28.5 * 0.85).toInt() = 24 (<= 25 -> budgetLimit = 60% of budget)
        val player = player(pts = 20.0, reb = 10.0, ast = 5.0, stl = 2.0, blk = 1.0)

        val valuation = useCase.evaluate(player, round = 0, aiBudget = 50, currentBid = 0)

        assertEquals(24, valuation.personalValuation)
        assertEquals(24, valuation.maxBid)
        assertTrue(valuation.canBid)
    }

    @Test
    fun evaluate_highValuationUsesFullBudgetAsLimitInsteadOfSixtyPercent() {
        // Same seed/randomFactor as above (round=0, aiBudget=50 -> 0.85), but a much stronger
        // stat line pushes personalValuation past the 25 threshold.
        // baseValuation = 30*0.8 + 10*0.5 + 5*0.6 + 2*1.5 + 1*1.5 = 36.5 -> personalValuation = 31
        val player = player(pts = 30.0, reb = 10.0, ast = 5.0, stl = 2.0, blk = 1.0)

        val valuation = useCase.evaluate(player, round = 0, aiBudget = 50, currentBid = 0)

        assertEquals(31, valuation.personalValuation)
        assertEquals(31, valuation.maxBid) // budgetLimit = aiBudget (50) here, so maxBid = valuation
        assertTrue(valuation.canBid)
    }

    @Test
    fun evaluate_cannotBidWhenBudgetAlreadyExhaustedByCurrentBid() {
        val player = player(pts = 20.0, reb = 10.0, ast = 5.0, stl = 2.0, blk = 1.0)

        val valuation = useCase.evaluate(player, round = 0, aiBudget = 10, currentBid = 10)

        assertFalse(valuation.canBid)
    }

    @Test
    fun evaluate_cannotBidWhenValuationCapIsBelowCurrentBidEvenWithBudgetLeft() {
        // aiBudget (15) > currentBid (10), but the 60%-of-budget cap (9) sits below currentBid.
        val player = player(pts = 20.0, reb = 10.0, ast = 5.0, stl = 2.0, blk = 1.0)

        val valuation = useCase.evaluate(player, round = 0, aiBudget = 15, currentBid = 10)

        assertEquals(9, valuation.maxBid)
        assertFalse(valuation.canBid)
    }

    // --- nextBidAmount ---

    @Test
    fun nextBidAmount_staysWithinCurrentBidExclusiveAndMaxBidInclusive() {
        val valuation = ComputerBidUseCase.Valuation(personalValuation = 24, maxBid = 24, canBid = true)

        repeat(50) {
            val next = useCase.nextBidAmount(valuation, currentBid = 10, aiBudget = 40)
            assertTrue("expected > 10, was $next", next > 10)
            assertTrue("expected <= 24, was $next", next <= 24)
        }
    }

    @Test
    fun nextBidAmount_neverExceedsMaxBidEvenWhenJumpWouldOvershoot() {
        // maxBid sits just one dollar above currentBid: even the largest possible jump must clamp.
        val valuation = ComputerBidUseCase.Valuation(personalValuation = 100, maxBid = 11, canBid = true)

        repeat(50) {
            val next = useCase.nextBidAmount(valuation, currentBid = 10, aiBudget = 40)
            assertEquals(11, next)
        }
    }

    // --- delays (bornées, car tirées au hasard) ---

    @Test
    fun thinkingDelayMillis_isLongerWhenOpeningTheAuction() {
        repeat(20) {
            val openingDelay = useCase.thinkingDelayMillis(currentBid = 0)
            val followUpDelay = useCase.thinkingDelayMillis(currentBid = 5)
            assertTrue(openingDelay in 3400L..5000L)
            assertTrue(followUpDelay in 2200L..3800L)
        }
    }

    @Test
    fun passDelayMillis_isWithinExpectedRange() {
        repeat(20) {
            assertTrue(useCase.passDelayMillis() in 800L..1500L)
        }
    }
}
