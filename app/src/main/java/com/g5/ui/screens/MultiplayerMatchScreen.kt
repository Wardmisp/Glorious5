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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.domain.model.TeamEntry
import com.g5.ui.components.BidControl
import com.g5.ui.components.PlayerRevealCard
import com.g5.ui.viewmodel.MatchUiState

@Composable
fun MultiplayerMatchScreen(
    state: MatchUiState,
    onBack: () -> Unit,
    onBidInputChange: (Int) -> Unit,
    onPlaceBid: () -> Unit,
    onPass: () -> Unit,
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
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "MATCH EN LIGNE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!state.isRealtimeConnected) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Reconnexion...",
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

                match.status == "waiting" -> WaitingForOpponent(matchId = match.id)

                match.status == "drafting" -> DraftingContent(
                    state = state,
                    onBidInputChange = onBidInputChange,
                    onPlaceBid = onPlaceBid,
                    onPass = onPass
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
            text = "EN ATTENTE D'UN ADVERSAIRE",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Partage ce code à un ami pour qu'il te rejoigne, ou attends qu'un joueur le trouve dans le lobby.",
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
                text = "  COPIER LE CODE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun DraftingContent(
    state: MatchUiState,
    onBidInputChange: (Int) -> Unit,
    onPlaceBid: () -> Unit,
    onPass: () -> Unit
) {
    val auction = state.currentAuction
    val player = state.currentPlayer
    val myBudget = state.myTeam?.budgetRemaining ?: 0
    val opponentBudget = state.opponentTeam?.budgetRemaining ?: 0

    val revealOrder = remember(auction?.id) { generateRevealOrder() }

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
            TurnBanner(state = state)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BidControl(
                    name = "Toi",
                    budget = myBudget,
                    value = state.bidInput,
                    onChange = onBidInputChange,
                    minBid = auction.currentBid + 1,
                    onBid = onPlaceBid,
                    leading = auction.currentBidderId == state.myUserId,
                    disabled = !state.isMyTurn || state.isSubmittingBid,
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
                        text = "Adversaire",
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
                        text = if (state.isMyTurn) "En attente de ta mise" else "Réfléchit...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Button(
                onClick = onPass,
                enabled = state.isMyTurn && state.canPass && !state.isSubmittingBid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PASSER",
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
private fun TurnBanner(state: MatchUiState) {
    val auction = state.currentAuction ?: return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (state.isMyTurn) Color(0xFFF4722B).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (state.isMyTurn) Color(0xFFF4722B).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isMyTurn) "À TOI DE MISER" else "AU TOUR DE L'ADVERSAIRE",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = if (state.isMyTurn) Color(0xFFF4722B) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (auction.currentBidderId != null) "Mise actuelle : $${auction.currentBid}" else "Aucune mise",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
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
            text = "Équipes (${myRoster.size + opponentRoster.size}/${teamSize * 2})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RosterColumn(name = "Toi", entries = myRoster, modifier = Modifier.weight(1f))
            RosterColumn(name = "Adversaire", entries = opponentRoster, modifier = Modifier.weight(1f))
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
