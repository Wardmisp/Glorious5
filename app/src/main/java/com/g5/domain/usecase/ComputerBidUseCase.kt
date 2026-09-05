package com.g5.domain.usecase

import com.g5.domain.model.BUDGET
import com.g5.domain.model.NBAPlayer

/**
 * Valorisation et décision de mise de l'IA en mode "vs ordinateur". Isolé de GameViewModel pour
 * que l'heuristique (et ses constantes magiques) soit testable sans dépendre du timing/coroutines
 * de l'écran.
 */
class ComputerBidUseCase {

    data class Valuation(val personalValuation: Int, val maxBid: Int, val canBid: Boolean)

    fun evaluate(player: NBAPlayer, round: Int, aiBudget: Int, currentBid: Int): Valuation {
        val baseValuation = player.pts * 0.8 +
            player.reb * 0.5 +
            player.ast * 0.6 +
            player.stl * 1.5 +
            player.blk * 1.5

        val seed = round + (aiBudget / 10)
        val randomFactor = 0.8 + (Math.abs(seed.hashCode() % 40) / 100.0)
        val personalValuation = (baseValuation * randomFactor).toInt()

        val budgetLimit = if (personalValuation > 25) aiBudget else (aiBudget * 0.6).toInt()
        val maxBid = minOf(personalValuation, budgetLimit)
        val canBid = aiBudget > currentBid && maxBid > currentBid

        return Valuation(personalValuation, maxBid, canBid)
    }

    /** Prochaine enchère de l'IA, une fois qu'on sait qu'elle peut (et veut) miser. */
    fun nextBidAmount(valuation: Valuation, currentBid: Int, aiBudget: Int): Int {
        val budgetRatio = aiBudget.toFloat() / BUDGET
        val interestRatio = valuation.personalValuation.toFloat() / 25f
        val maxJump = when {
            interestRatio > 0.9f && budgetRatio > 0.7f -> 5
            interestRatio > 0.7f && budgetRatio > 0.4f -> 3
            interestRatio > 0.5f -> 2
            else -> 1
        }
        val jump = (1..maxJump).random()
        return minOf(currentBid + jump, valuation.maxBid)
    }

    /** Délai de "réflexion" avant de miser — purement cosmétique (fait sentir une IA qui hésite). */
    fun thinkingDelayMillis(currentBid: Int): Long {
        val baseDelay = if (currentBid == 0) 2400L else 1200L
        val extraDelay = (1000..2600).random().toLong()
        return baseDelay + extraDelay
    }

    /** Délai avant de passer, plus court : l'IA "voit" tout de suite qu'elle ne peut pas suivre. */
    fun passDelayMillis(): Long = (800L..1500L).random()
}
