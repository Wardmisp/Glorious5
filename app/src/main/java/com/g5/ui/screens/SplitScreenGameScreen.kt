package com.g5.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.g5.R
import com.g5.data.local.NBA_PLAYERS
import com.g5.domain.model.TOTAL
import com.g5.domain.model.TeamEntry
import com.g5.ui.components.PlayerRevealCard
import com.g5.ui.util.positionLabel
import com.g5.ui.viewmodel.GameViewModel

@Composable
fun SplitScreenGameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState = viewModel.uiState.value.gameState
    val totalPlayers = if (gameState.players.isNotEmpty()) gameState.players.size else TOTAL
    val player = gameState.players.getOrNull(gameState.round)
        ?: NBA_PLAYERS[gameState.round.coerceIn(0, NBA_PLAYERS.size - 1)]

    val minBid = gameState.bid + 1
    val p1Budget = gameState.budgets.first
    val p2Budget = gameState.budgets.second
    val p1Full = gameState.teams.first.size >= TOTAL / 2
    val p2Full = gameState.teams.second.size >= TOTAL / 2

    val targetRotation = when {
        gameState.done -> if (gameState.awardedTo == 2) 180f else 0f
        gameState.activePlayerTurn == 2 -> 180f
        else -> 0f
    }
    val cardRotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 400),
        label = "playerCardRotation"
    )

    if (gameState.gameOver) {
        GameOverScreen(
            teams = gameState.teams,
            budgets = gameState.budgets,
            analytics = gameState.analytics,
            luckyWinner = gameState.luckyWinner,
            p1Name = stringResource(R.string.common_player_number, 1),
            p2Name = stringResource(R.string.common_player_number, 2),
            onBack = onBack,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // ZONE JOUEUR 2 (Haut de l'écran, inversé à 180°)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(180f)
            ) {
                PlayerControlSection(
                    playerName = stringResource(R.string.common_player_number_upper, 2),
                    playerNumber = 2,
                    budget = p2Budget,
                    teamCount = gameState.teams.second.size,
                    isActive = gameState.activePlayerTurn == 2 && !gameState.done,
                    isDone = gameState.done,
                    isFull = p2Full,
                    minBid = minBid,
                    currentBidInput = gameState.p2Input,
                    onBidInputChange = { viewModel.setP2Input(it) },
                    onBid = { viewModel.handleP2Bid(minBid, p2Budget) },
                    onPass = { viewModel.passP2() },
                    onOpenTeam = { viewModel.toggleP2Team() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // ZONE CENTRALE (Carte Joueur & Statut Enchère)
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barre d'en-tête centrale
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            .clickable {
                                viewModel.onExitGameScreen()
                                onBack()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.split_header_title, gameState.round + 1, totalPlayers),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp,
                        color = Color(0xFFF4722B)
                    )

                    // Spacer pour équilibrer la ligne
                    Spacer(modifier = Modifier.size(32.dp))
                }

                // Carte Joueur (orientée dynamiquement vers le joueur actif)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rotate(cardRotation),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerRevealCard(
                        player = player,
                        bidCount = gameState.bidCount,
                        revealOrder = gameState.revealOrder,
                        revealed = gameState.done
                    )
                }

                // Statut de l'enchère
                if (!gameState.done) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (gameState.bid > 0) {
                                    val leader = stringResource(R.string.common_player_number, if (gameState.bidder == 1) 1 else 2)
                                    stringResource(R.string.split_current_bid, gameState.bid, leader)
                                } else {
                                    stringResource(R.string.split_no_bid)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (gameState.bid > 0) Color(0xFFF4722B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = stringResource(R.string.split_next_min_bid, minBid),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    // Bannière de résultat de l'enchère
                    val winnerName = stringResource(R.string.common_player_number, if (gameState.awardedTo == 1) 1 else 2)
                    val isLastRound = gameState.round + 1 >= totalPlayers
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.split_player_won, winnerName, player.displayLastName, gameState.bid),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Button(
                            onClick = { viewModel.nextRound() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4722B))
                        ) {
                            Text(
                                text = stringResource(if (isLastRound) R.string.split_see_scouting else R.string.split_next_player),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // ZONE JOUEUR 1 (Bas de l'écran, orientation normale)
            // ==========================================
            Box(modifier = Modifier.fillMaxWidth()) {
                PlayerControlSection(
                    playerName = stringResource(R.string.common_player_number_upper, 1),
                    playerNumber = 1,
                    budget = p1Budget,
                    teamCount = gameState.teams.first.size,
                    isActive = gameState.activePlayerTurn == 1 && !gameState.done,
                    isDone = gameState.done,
                    isFull = p1Full,
                    minBid = minBid,
                    currentBidInput = gameState.p1Input,
                    onBidInputChange = { viewModel.setP1Input(it) },
                    onBid = { viewModel.handleP1Bid(minBid, p1Budget) },
                    onPass = { viewModel.passP1() },
                    onOpenTeam = { viewModel.toggleP1Team() }
                )
            }
        }

        // ==========================================
        // DIALOGUES D'ÉQUIPE (POUR CHAQUE JOUEUR)
        // ==========================================
        if (gameState.showP1Team) {
            PlayerTeamDialog(
                playerName = stringResource(R.string.common_player_number_upper, 1),
                budget = p1Budget,
                team = gameState.teams.first,
                onDismiss = { viewModel.toggleP1Team() },
                isRotated = false
            )
        }

        if (gameState.showP2Team) {
            PlayerTeamDialog(
                playerName = stringResource(R.string.common_player_number_upper, 2),
                budget = p2Budget,
                team = gameState.teams.second,
                onDismiss = { viewModel.toggleP2Team() },
                isRotated = true
            )
        }
    }
}

@Composable
private fun PlayerControlSection(
    playerName: String,
    playerNumber: Int,
    budget: Int,
    teamCount: Int,
    isActive: Boolean,
    isDone: Boolean,
    isFull: Boolean,
    minBid: Int,
    currentBidInput: Int,
    onBidInputChange: (Int) -> Unit,
    onBid: () -> Unit,
    onPass: () -> Unit,
    onOpenTeam: () -> Unit
) {
    val effectiveBid = maxOf(currentBidInput, minBid)
    val canBid = isActive && !isDone && !isFull && effectiveBid <= budget

    val borderColor = if (isActive) Color(0xFFF4722B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val backgroundColor = if (isActive) {
        Color(0xFFF4722B).copy(alpha = 0.06f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(14.dp))
            .border(width = if (isActive) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Ligne d'en-tête du joueur : Nom, Budget, Effectif, Bouton Équipe
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    color = if (isActive) Color(0xFFF4722B) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "· $${budget}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFF4722B)
                )
            }

            // Bouton Mon Équipe
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onOpenTeam() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = stringResource(R.string.common_teams),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.split_team_button, teamCount),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Badge d'état du tour
        val statusText = when {
            isDone -> stringResource(R.string.split_status_round_over)
            isFull -> stringResource(R.string.split_status_full)
            isActive -> stringResource(R.string.split_status_your_turn)
            else -> stringResource(R.string.split_status_waiting_for, if (playerNumber == 1) 2 else 1)
        }

        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            color = if (isActive) Color(0xFFF4722B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        // Contrôles d'enchères et Passer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton PASSER
            OutlinedButton(
                onClick = onPass,
                enabled = isActive && !isDone,
                modifier = Modifier
                    .weight(0.9f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.split_pass_button),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
            }

            // Stepper : Diminuer
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = if (isActive && !isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isActive && !isDone) {
                        onBidInputChange(maxOf(minBid, effectiveBid - 1))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.common_decrease),
                    tint = if (isActive && !isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Affichage du montant
            Text(
                text = "$${effectiveBid}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            // Stepper : Augmenter
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = if (isActive && !isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = isActive && !isDone) {
                        onBidInputChange(minOf(budget, effectiveBid + 1))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.common_increase),
                    tint = if (isActive && !isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Bouton ENCHÉRIR
            Button(
                onClick = onBid,
                enabled = canBid,
                modifier = Modifier
                    .weight(1.1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4722B))
            ) {
                Text(
                    text = stringResource(R.string.split_bid_button),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PlayerTeamDialog(
    playerName: String,
    budget: Int,
    team: List<TeamEntry>,
    onDismiss: () -> Unit,
    isRotated: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isRotated) Modifier.rotate(180f) else Modifier)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header du dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.split_dialog_title, playerName),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFFF4722B)
                        )
                        Text(
                            text = stringResource(R.string.split_dialog_subtitle, budget, team.size),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.split_dialog_close),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Liste des joueurs recrutés
                if (team.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.split_dialog_empty),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        team.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = Color(0xFFF4722B)
                                    )
                                    Column {
                                        Text(
                                            text = "${entry.player.displayFirstName} ${entry.player.displayLastName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.split_dialog_player_line, positionLabel(entry.player.position), entry.player.team, entry.player.season),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Text(
                                    text = "$${entry.paid}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = Color(0xFFF4722B)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.split_dialog_close),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}
