package com.g5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.R
import com.g5.domain.model.TeamEntry
import com.g5.ui.viewmodel.MatchUiState

/**
 * Le vainqueur est déjà décidé côté serveur (compute_match_result, tirage
 * pondéré via calc_team_score). On affiche ce résultat tel quel, sans le
 * recalculer localement (les use cases locaux sont un algorithme différent
 * et pourraient contredire la vraie décision du serveur).
 */
@Composable
fun MultiplayerResultScreen(
    state: MatchUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match = state.match
    val iWon = match?.winnerId != null && match.winnerId == state.myUserId
    val isDraw = match?.winnerId == null

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.mp_result_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(
                        when {
                            isDraw -> R.string.mp_result_draw
                            iWon -> R.string.mp_result_you_won
                            else -> R.string.mp_result_opponent_won
                        }
                    ),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFF4722B)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResultColumn(
                    name = stringResource(R.string.common_you),
                    entries = state.myRoster,
                    score = state.myTeam?.totalScore,
                    budgetLeft = state.myTeam?.budgetRemaining ?: 0,
                    isWinner = iWon,
                    modifier = Modifier.weight(1f)
                )
                ResultColumn(
                    name = stringResource(R.string.common_opponent),
                    entries = state.opponentRoster,
                    score = state.opponentTeam?.totalScore,
                    budgetLeft = state.opponentTeam?.budgetRemaining ?: 0,
                    isWinner = !iWon && !isDraw,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.common_back_to_menu),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ResultColumn(
    name: String,
    entries: List<TeamEntry>,
    score: Double?,
    budgetLeft: Int,
    isWinner: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = if (isWinner) Color(0xFFF59E0B).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(if (isWinner) R.string.common_winner else R.string.common_loser),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 1.sp,
            color = if (isWinner) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        if (score != null) {
            Text(
                text = stringResource(R.string.mp_result_score, "%.1f".format(score)),
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(color = Color(0xFFF4722B), shape = RoundedCornerShape(2.dp))
                    )
                    Text(
                        text = entry.player.displayLastName,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$${entry.paid}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.game_over_budget_left, budgetLeft),
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
