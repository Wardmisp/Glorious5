package com.g5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.R
import com.g5.domain.model.TeamEntry
import com.g5.ui.components.BidControl
import com.g5.ui.components.PlayerRevealCard
import com.g5.domain.model.CompletedAuctionInfo
import com.g5.ui.viewmodel.MatchUiState
import kotlinx.coroutines.delay

@Composable
fun MultiplayerMatchScreen(
    state: MatchUiState,
    onBack: () -> Unit,
    onBidInputChange: (Int) -> Unit,
    onPlaceBid: () -> Unit,
    onPass: () -> Unit,
    onTimerExpired: () -> Unit,
    onDismissPendingResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match = state.match

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = stringResource(R.string.mp_match_title),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!state.isRealtimeConnected) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = stringResource(R.string.mp_match_reconnecting),
                    tint = Color(0xFFE03A3E),
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Box(modifier = Modifier.size(16.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.error?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFE03A3E)
                )
            }

            when {
                match == null -> Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFFF4722B)) }

                // Prioritaire sur tout le reste : tant que le joueur n'a pas fermé cet écran,
                // on ne bascule pas vers l'enchère suivante (ou le résultat) même si elle est
                // déjà prête côté serveur — évite que l'écran change brusquement de joueur.
                state.pendingResult != null -> AuctionResultBuffer(
                    result = state.pendingResult,
                    onContinue = onDismissPendingResult
                )

                match.status == "waiting" -> WaitingForOpponent(matchId = match.id)

                match.status == "drafting" -> DraftingContent(
                    state = state,
                    onBidInputChange = onBidInputChange,
                    onPlaceBid = onPlaceBid,
                    onPass = onPass,
                    onTimerExpired = onTimerExpired
                )

                else -> Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Color(0xFFF4722B)) }
            }
        }
    }
}

/** Même logique que GameViewModel.generateRevealOrder() côté mode local, dupliquée ici pour ne
 * pas dépendre de GameViewModel (qui appartient au mode local, non touché par le mode en ligne). */
private fun generateRevealOrder(): List<Int> {
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

@Composable
private fun AuctionResultBuffer(result: CompletedAuctionInfo, onContinue: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PlayerRevealCard(
            player = result.player,
            bidCount = 0,
            revealOrder = (0..9).toList(),
            revealed = true
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF59E0B).copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = Color(0xFFF59E0B).copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                val who = if (result.isAutoAssigned) {
                    stringResource(if (result.winnerIsMe) R.string.mp_match_you_auto_win else R.string.mp_match_opponent_auto_win)
                } else {
                    stringResource(if (result.winnerIsMe) R.string.mp_match_you_win else R.string.mp_match_opponent_win)
                }
                val price = if (result.pricePaid == 0) stringResource(R.string.mp_match_for_free) else stringResource(R.string.mp_match_for_price, result.pricePaid)
                Text(
                    text = stringResource(R.string.mp_match_won_announcement, who, result.player.displayLastName, price),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(
                text = stringResource(if (result.isLastPick) R.string.mp_match_see_results else R.string.mp_match_next_player),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun WaitingForOpponent(matchId: String) {
    val clipboard = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(color = Color(0xFFF4722B))

        Text(
            text = stringResource(R.string.mp_match_waiting_for_opponent_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.mp_match_share_code_hint),
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Text(
            text = matchId,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFF4722B),
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        OutlinedButton(
            onClick = { clipboard.setText(AnnotatedString(matchId)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = stringResource(R.string.mp_match_copy_code),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        }
    }
}

private fun secondsRemaining(deadlineMillis: Long?): Int? {
    if (deadlineMillis == null) return null
    val remainingMs = deadlineMillis - System.currentTimeMillis()
    return (remainingMs / 1000L).toInt().coerceIn(0, 15)
}

@Composable
private fun DraftingContent(
    state: MatchUiState,
    onBidInputChange: (Int) -> Unit,
    onPlaceBid: () -> Unit,
    onPass: () -> Unit,
    onTimerExpired: () -> Unit
) {
    val auction = state.currentAuction
    val player = state.currentPlayer
    val myBudget = state.myTeam?.budgetRemaining ?: 0
    val opponentBudget = state.opponentTeam?.budgetRemaining ?: 0

    val revealOrder = remember(auction?.id) { generateRevealOrder() }

    val deadline = state.turnDeadlineAtMillis
    var timeLeft by remember(auction?.id, deadline) { mutableStateOf(secondsRemaining(deadline)) }
    LaunchedEffect(auction?.id, deadline) {
        if (deadline == null) return@LaunchedEffect
        while (true) {
            val remaining = secondsRemaining(deadline)
            timeLeft = remaining
            if (remaining != null && remaining <= 0) {
                // N'importe quel client peut déclencher la résolution côté serveur (voir
                // expire_turn_if_overdue) — utile si c'est l'adversaire qui est hors ligne.
                // On continue de réessayer (pas de "break") : la RPC est un no-op si le
                // serveur n'est pas encore d'accord que le délai est dépassé (léger décalage
                // d'horloge entre l'appareil et Supabase) ou si l'appel réseau échoue — sans
                // retry, un seul essai raté laissait le tour bloqué indéfiniment.
                onTimerExpired()
            }
            delay(500)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (player != null) {
            PlayerRevealCard(
                player = player,
                bidCount = state.bidCount,
                revealOrder = revealOrder,
                revealed = false
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFFF4722B)) }
        }

        if (auction != null) {
            TurnBanner(state = state, secondsLeft = timeLeft)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BidControl(
                    name = stringResource(R.string.common_you),
                    budget = myBudget,
                    value = state.bidInput,
                    onChange = onBidInputChange,
                    minBid = auction.currentBid + 1,
                    onBid = onPlaceBid,
                    leading = auction.currentBidderId == state.myUserId,
                    disabled = !state.isMyTurn || state.isSubmittingBid || state.isAutoPassing || state.cannotAffordNextBid,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.common_opponent),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${opponentBudget}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFFF4722B)
                    )
                    Text(
                        text = stringResource(if (state.isMyTurn) R.string.mp_match_waiting_your_bid else R.string.mp_match_opponent_thinking),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Button(
                onClick = onPass,
                enabled = state.isMyTurn && state.canPass && !state.isSubmittingBid && !state.isAutoPassing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(if (state.isAutoPassing) R.string.mp_match_auto_passing else R.string.mp_match_pass_button),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        RosterSummary(
            myRoster = state.myRoster,
            opponentRoster = state.opponentRoster,
            teamSize = state.match?.teamSize ?: 5
        )
    }
}

@Composable
private fun TurnBanner(state: MatchUiState, secondsLeft: Int?) {
    val auction = state.currentAuction ?: return
    val isOpening = auction.currentBidderId == null
    val isAutoPassing = state.isAutoPassing || (state.isMyTurn && state.cannotAffordNextBid)
    val turnLabel = stringResource(
        when {
            isAutoPassing -> R.string.mp_match_insufficient_budget
            state.isMyTurn && isOpening -> R.string.mp_match_you_open
            state.isMyTurn -> R.string.mp_match_your_turn
            isOpening -> R.string.mp_match_opponent_opens
            else -> R.string.mp_match_opponent_turn
        }
    )

    val bannerColor = when {
        isAutoPassing -> Color(0xFFE03A3E)
        state.isMyTurn -> Color(0xFFF4722B)
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = bannerColor?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = bannerColor?.copy(alpha = 0.4f) ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = turnLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = bannerColor ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (!isOpening) stringResource(R.string.mp_match_current_bid, auction.currentBid) else stringResource(R.string.auction_banner_no_bid),
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (secondsLeft != null) {
            val urgent = secondsLeft <= 5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = (if (urgent) Color(0xFFE03A3E) else Color(0xFFF4722B)).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${secondsLeft}s",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (urgent) Color(0xFFE03A3E) else Color(0xFFF4722B)
                    )
                }
            }
        }
    }
}

@Composable
private fun RosterSummary(myRoster: List<TeamEntry>, opponentRoster: List<TeamEntry>, teamSize: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.mp_match_roster_summary, myRoster.size + opponentRoster.size, teamSize * 2),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RosterColumn(name = stringResource(R.string.common_you), entries = myRoster, modifier = Modifier.weight(1f))
            RosterColumn(name = stringResource(R.string.common_opponent), entries = opponentRoster, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RosterColumn(name: String, entries: List<TeamEntry>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFF4722B)
                )
            }
        }
    }
}
