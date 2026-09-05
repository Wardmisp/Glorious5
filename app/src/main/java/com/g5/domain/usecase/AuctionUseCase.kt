package com.g5.domain.usecase

import com.g5.domain.model.GameState
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TOTAL
import com.g5.domain.model.TeamEntry

data class PassOutcome(val awardedTo: Int, val finalBid: Int)

/**
 * Règles métier d'un tour d'enchère en mode local (IA / hotseat) : qui remporte un joueur quand
 * un camp passe, et le règlement budget/effectif qui en découle. Extrait de GameViewModel pour
 * rester testable indépendamment de l'état d'écran (navigation, son, timer).
 */
class AuctionUseCase {

    private val teamSlots get() = TOTAL / 2

    fun generateRevealOrder(): List<Int> {
        val baseWeights = listOf(
            100, // 0: PTS
            55,  // 1: REB
            75,  // 2: AST
            30,  // 3: STL
            10,  // 4: BLK
            5,   // 5: Season
            20,  // 6: Position
            150, // 7: Team
            200, // 8: FirstName
            250  // 9: LastName
        )
        return (0..9).map { index ->
            index to (baseWeights[index] + ((-10..10).random()))
        }.sortedBy { it.second }.map { it.first }
    }

    /** Nouvel état pour le round donné, en conservant budgets/effectifs déjà constitués. */
    fun startRound(previous: GameState, roundIndex: Int): GameState {
        val starter = (roundIndex % 2) + 1
        return GameState(
            round = roundIndex,
            bid = 0,
            bidder = null,
            p1Input = 1,
            p2Input = 1,
            budgets = previous.budgets,
            teams = previous.teams,
            bidCount = 0,
            revealOrder = generateRevealOrder(),
            timer = 15,
            players = previous.players,
            activePlayerTurn = starter,
            isVsHuman = previous.isVsHuman
        )
    }

    fun isTeamFull(state: GameState, player: Int): Boolean {
        val team = if (player == 1) state.teams.first else state.teams.second
        return team.size >= teamSlots
    }

    /** Décide qui remporte le joueur en vente quand [passedBy] passe, ou `null` si les deux
     * effectifs sont déjà complets (ne devrait pas arriver, cf. appelant). */
    fun resolvePass(state: GameState, passedBy: Int): PassOutcome? {
        val recipient = if (passedBy == 1) 2 else 1
        val recipientFull = isTeamFull(state, recipient)
        val passerFull = isTeamFull(state, passedBy)

        return when {
            !recipientFull -> {
                val finalBid = if (state.bidder == recipient) {
                    state.bid
                } else {
                    if (passerFull) 0 else maxOf(1, state.bid)
                }
                PassOutcome(recipient, finalBid)
            }
            !passerFull -> {
                val finalBid = if (state.bidder == passedBy) state.bid else 0
                PassOutcome(passedBy, finalBid)
            }
            else -> null
        }
    }

    /** Attribue [player] à [bidder] pour [bid] (plafonné par son budget restant) et met à jour
     * budgets/effectifs. */
    fun adjudicate(state: GameState, player: NBAPlayer, bid: Int, bidder: Int): GameState {
        val currentBudget = if (bidder == 1) state.budgets.first else state.budgets.second
        val actualCost = minOf(bid, currentBudget)

        val newBudgets = if (bidder == 1) {
            state.budgets.first - actualCost to state.budgets.second
        } else {
            state.budgets.first to state.budgets.second - actualCost
        }

        val newTeams = if (bidder == 1) {
            (state.teams.first + TeamEntry(player, actualCost)) to state.teams.second
        } else {
            state.teams.first to (state.teams.second + TeamEntry(player, actualCost))
        }

        return state.copy(
            bid = actualCost,
            budgets = newBudgets,
            teams = newTeams,
            awardedTo = bidder,
            done = true,
            thinking = false
        )
    }
}
