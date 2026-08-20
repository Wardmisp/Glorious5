package com.g5.domain.usecase

import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.PlayerScore
import com.g5.domain.model.TeamAnalytics
import kotlin.math.exp

class CalculateWinProbabilityUseCase {

    fun execute(
        teamA: List<NBAPlayer>,
        teamB: List<NBAPlayer>,
        allSeasons: List<NBAPlayer>
    ): Pair<TeamAnalytics, TeamAnalytics> {
        val scoredTeamA = teamA.map { calculatePlayerScore(it, allSeasons) }
        val scoredTeamB = teamB.map { calculatePlayerScore(it, allSeasons) }

        val scoreA = calculateTeamScore(scoredTeamA)
        val scoreB = calculateTeamScore(scoredTeamB)

        // Logistic win probability function: P(A) = 1 / (1 + e^-(D/8)) where D = TeamA - TeamB
        val diff = scoreA - scoreB
        val probA = 1.0 / (1.0 + exp(-diff / 8.0))
        val probB = 1.0 - probA

        return Pair(
            TeamAnalytics(scoreA, probA, scoredTeamA),
            TeamAnalytics(scoreB, probB, scoredTeamB)
        )
    }

    private fun calculatePlayerScore(player: NBAPlayer, distribution: List<NBAPlayer>): PlayerScore {
        val pPts = getPercentile(player.pts, distribution.map { it.pts })
        val pReb = getPercentile(player.reb, distribution.map { it.reb })
        val pAst = getPercentile(player.ast, distribution.map { it.ast })
        val pStl = getPercentile(player.stl, distribution.map { it.stl })
        val pBlk = getPercentile(player.blk, distribution.map { it.blk })
        val pFg = getPercentile(player.fgPct, distribution.map { it.fgPct })
        val p3p = getPercentile(player.fg3Pct, distribution.map { it.fg3Pct })
        val pFt = getPercentile(player.ftPct, distribution.map { it.ftPct })
        val pPer = getPercentile(player.per, distribution.map { it.per })
        
        // WS/Game calculation
        val wsPerGame = if (player.games > 0) player.winShares / player.games else 0.0
        val wsDistribution = distribution.map { if (it.games > 0) it.winShares / it.games else 0.0 }
        val pWs = getPercentile(wsPerGame, wsDistribution)

        // EFF = 0.40*PFG + 0.35*P3P + 0.25*PFT
        val eff = 0.40 * pFg + 0.35 * p3p + 0.25 * pFt
        // IMPACT = 0.60*PPER + 0.40*PWS/Game
        val impact = 0.60 * pPer + 0.40 * pWs

        // PlayerScore = 0.20*PPTS + 0.10*PREB + 0.15*PAST + 0.10*PSTL + 0.10*PBLK + 0.10*EFF + 0.25*IMPACT
        val totalScore = 0.20 * pPts + 0.10 * pReb + 0.15 * pAst + 0.10 * pStl + 0.10 * pBlk + 0.10 * eff + 0.25 * impact

        return PlayerScore(
            player, pPts, pReb, pAst, pStl, pBlk, pFg, p3p, pFt, pPer, pWs, eff, impact, totalScore
        )
    }

    private fun getPercentile(value: Double, distribution: List<Double>): Double {
        if (distribution.isEmpty()) return 0.0
        val countBelow = distribution.count { it < value }
        val countEqual = distribution.count { it == value }
        return ((countBelow + 0.5 * countEqual) / distribution.size) * 100.0
    }

    private fun calculateTeamScore(players: List<PlayerScore>): Double {
        if (players.isEmpty()) return 0.0
        
        // Positional weights: PG: 0.22, SG: 0.20, SF: 0.20, PF: 0.20, C: 0.18
        val posWeights = mapOf(
            "Meneur" to 0.22,
            "Arrière" to 0.20,
            "Ailier" to 0.20,
            "Ailier Fort" to 0.20,
            "Pivot" to 0.18,
            "Arrière-Ailier" to 0.20,
            "Intérieur" to 0.19,
            "Polyvalent" to 0.20
        )

        var totalWeight = 0.0
        var weightedSum = 0.0

        players.forEach { scored ->
            val weight = posWeights[scored.player.position] ?: 0.20
            weightedSum += scored.totalScore * weight
            totalWeight += weight
        }

        // We normalize so the total weight corresponds to a 5-player team scale
        // The formula 0.22*PG... sums to 1.0. 
        // If we have 5 players, the sum of weights will be around 1.0.
        // We return the weighted sum directly as the "Team Score"
        return weightedSum
    }
}
