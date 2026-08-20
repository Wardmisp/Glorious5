package com.g5.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.domain.model.GameAction
import com.g5.ui.components.StatusBar
import com.g5.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SimulationScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState = viewModel.uiState.value.gameState
    val currentQuarter = gameState.currentSimulationQuarter
    val simulation = gameState.matchSimulation
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // On attend un peu avant de commencer le 1er quart-temps
        if (currentQuarter == 0) {
            delay(1500)
            viewModel.advanceSimulation()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StatusBar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MATCH EN COURS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp,
                color = Color(0xFFF4722B)
            )

            // Quart-temps s'affichant au fur et à mesure
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                simulation.take(currentQuarter).forEachIndexed { index, quarter ->
                    val isLatestRevealed = (index + 1) == currentQuarter
                    QuarterCard(
                        number = quarter.quarterNumber,
                        actions = quarter.actions,
                        onAllFinished = {
                            if (isLatestRevealed) {
                                scope.launch {
                                    delay(2500) // Délai de confort entre les quarts
                                    viewModel.advanceSimulation()
                                }
                            }
                        }
                    )
                }
                
                if (currentQuarter < 4) {
                    CurrentQuarterLoading(currentQuarter + 1)
                }
            }
        }
    }
}

@Composable
fun QuarterCard(number: Int, actions: List<GameAction>, onAllFinished: () -> Unit = {}) {
    var visibleActionsCount by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFF4722B), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "${number}ER QUART-TEMPS",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        actions.forEachIndexed { index, action ->
            if (index < visibleActionsCount) {
                val backgroundColor = if (action.favorsTeamA) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f)
                val borderColor = if (action.favorsTeamA) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color(0xFFF44336).copy(alpha = 0.3f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
                        .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    TypewriterText(
                        text = action.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface,
                        onFinished = {
                            if (visibleActionsCount < actions.size) {
                                visibleActionsCount++
                            } else {
                                onAllFinished()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    color: Color,
    modifier: Modifier = Modifier,
    delayMillis: Long = 35L,
    onFinished: () -> Unit = {}
) {
    var textToDisplay by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        textToDisplay = ""
        text.forEachIndexed { index, _ ->
            textToDisplay = text.substring(0, index + 1)
            delay(delayMillis)
        }
        onFinished()
    }

    Text(
        text = textToDisplay,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        color = color,
        modifier = modifier
    )
}

@Composable
fun CurrentQuarterLoading(number: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Simulation du ${number}e quart-temps...",
            fontSize = 12.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}
