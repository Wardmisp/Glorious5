package com.g5.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.g5.shared.resources.*
import com.g5.domain.model.GameState
import com.g5.ui.viewmodel.MatchUiState

/**
 * Réutilise l'écran de scouting du mode local (analyse d'avant-match, probabilités,
 * duels par poste) pour le match en ligne terminé, avec des libellés "Toi"/"Adversaire"
 * au lieu de "Vous"/"IA".
 */
@Composable
fun MultiplayerScoutingScreen(
    state: MatchUiState,
    onStartSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = state.analytics ?: return
    ScoutingReportScreen(
        gameState = GameState(analytics = analytics),
        onStartSimulation = onStartSimulation,
        modifier = modifier,
        labelA = stringResource(Res.string.scouting_multiplayer_label_you),
        labelB = stringResource(Res.string.scouting_multiplayer_label_opponent)
    )
}
