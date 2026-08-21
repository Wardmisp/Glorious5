package com.g5.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.data.local.NBA_PLAYERS
import com.g5.data.local.TOTAL
import com.g5.ui.components.AuctionBanner
import com.g5.ui.components.BidControl
import com.g5.ui.components.ComputerPanel
import com.g5.ui.components.PlayerRevealCard
import com.g5.ui.components.StatusBar
import com.g5.ui.components.TeamsPanel
import com.g5.ui.viewmodel.GameViewModel

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun GameScreen(
    vsComputer: Boolean,
    viewModel: GameViewModel,
    onBack: () -> Unit,
    tutorialPositions: MutableMap<String, Rect> = mutableMapOf(),
    modifier: Modifier = Modifier
) {
    val gameState = viewModel.uiState.value.gameState
    val totalPlayers = if (gameState.players.isNotEmpty()) gameState.players.size else TOTAL
    val player = gameState.players.getOrNull(gameState.round)
        ?: NBA_PLAYERS[gameState.round.coerceIn(0, NBA_PLAYERS.size - 1)]
    val p1Name = if (vsComputer) "Vous" else "Joueur 1"
    val p2Name = if (vsComputer) "Ordi" else "Joueur 2"
    val minBid = gameState.bid + 1

    // Computer AI logic
    LaunchedEffect(gameState.bid, gameState.bidder, gameState.round, vsComputer) {
        if (!vsComputer || gameState.done || gameState.gameOver) return@LaunchedEffect
        if (gameState.bidder == 2) return@LaunchedEffect
        
        viewModel.computerBid(minBid)
    }

    if (gameState.gameOver) {
        GameOverScreen(
            teams = gameState.teams,
            budgets = gameState.budgets,
            analytics = gameState.analytics,
            luckyWinner = gameState.luckyWinner,
            p1Name = p1Name,
            p2Name = p2Name,
            onBack = {
                viewModel.goBack()
                onBack()
            },
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            StatusBar()

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
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

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ENCHÈRES NBA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Joueur ${gameState.round + 1} / $totalPlayers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.toggleTeamsPanel() }
                        .onGloballyPositioned { coords ->
                            tutorialPositions["game_teams"] = coords.boundsInRoot()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Équipes",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((gameState.round.toFloat() / totalPlayers.toFloat()))
                        .height(2.dp)
                        .background(color = Color(0xFFF4722B))
                )
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PlayerRevealCard(
                    player = player,
                    bidCount = gameState.bidCount,
                    revealOrder = gameState.revealOrder,
                    revealed = gameState.done,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        tutorialPositions["game_card"] = coords.boundsInRoot()
                    }
                )

                AuctionBanner(
                    currentBid = gameState.bid,
                    currentBidder = gameState.bidder,
                    p1Name = p1Name,
                    p2Name = p2Name,
                    thinking = gameState.thinking && vsComputer,
                    done = gameState.done,
                    awardedTo = gameState.awardedTo,
                    timer = gameState.timer,
                    modifier = Modifier.onGloballyPositioned { coords ->
                        tutorialPositions["game_timer"] = coords.boundsInRoot()
                    }
                )

                if (!gameState.done) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coords ->
                                tutorialPositions["game_bid"] = coords.boundsInRoot()
                            },
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val p1Full = gameState.teams.first.size >= TOTAL / 2
                            val p2Full = gameState.teams.second.size >= TOTAL / 2
                            
                            BidControl(
                                name = p1Name,
                                budget = gameState.budgets.first,
                                value = gameState.p1Input,
                                onChange = { viewModel.setP1Input(it) },
                                minBid = minBid,
                                onBid = { viewModel.handleP1Bid(minBid, gameState.budgets.first) },
                                leading = gameState.bidder == 1,
                                disabled = p1Full,
                                modifier = Modifier.weight(1f)
                            )

                            if (vsComputer) {
                                ComputerPanel(
                                    budget = gameState.budgets.second,
                                    leading = gameState.bidder == 2,
                                    thinking = gameState.thinking,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                BidControl(
                                    name = p2Name,
                                    budget = gameState.budgets.second,
                                    value = gameState.p2Input,
                                    onChange = { viewModel.setP2Input(it) },
                                    minBid = minBid,
                                    onBid = { viewModel.handleP2Bid(minBid, gameState.budgets.second) },
                                    leading = gameState.bidder == 2,
                                    disabled = p2Full,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        val p2Full = gameState.teams.second.size >= TOTAL / 2
                        val priceToOpponent = if (gameState.bidder == 2) gameState.bid else maxOf(1, gameState.bid)
                        val priceToMe = if (gameState.bidder == 1) gameState.bid else maxOf(1, gameState.bid)

                        Button(
                            onClick = { viewModel.pass() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .onGloballyPositioned { coords ->
                                    tutorialPositions["game_pass"] = coords.boundsInRoot()
                                }
                        ) {
                            Text(
                                text = if (p2Full) "RÉCUPÉRER LE JOUEUR ($priceToMe$)" else "PASSER (laisser à $p2Name pour $priceToOpponent$)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (gameState.awardedTo != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${if (gameState.awardedTo == 1) p1Name else p2Name} remporte ${player.lastName} pour $${gameState.bid} !",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.nextRound() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (gameState.round + 1 >= totalPlayers) "VOIR LES RÉSULTATS →" else "JOUEUR SUIVANT →",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                TeamsPanel(
                    teams = gameState.teams,
                    budgets = gameState.budgets,
                    p1Name = p1Name,
                    p2Name = p2Name,
                    open = gameState.showTeams,
                    onToggle = { viewModel.toggleTeamsPanel() }
                )
            }
        }
    }
}
