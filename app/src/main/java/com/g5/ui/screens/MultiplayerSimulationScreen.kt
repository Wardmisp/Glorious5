package com.g5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.g5.ui.components.GymClock
import com.g5.ui.viewmodel.MatchUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Équivalent en ligne de SimulationScreen (mode local), mais piloté par MultiplayerViewModel
 * plutôt que GameViewModel — réutilise QuarterCard/GymClock/CurrentQuarterLoading définis dans
 * SimulationScreen.kt (même package, pas de duplication).
 */
@Composable
fun MultiplayerSimulationScreen(
    state: MatchUiState,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuarter = state.currentSimulationQuarter
    val simulation = state.matchSimulation
    val scope = rememberCoroutineScope()

    var secondsRemaining by remember { mutableStateOf(720) }

    LaunchedEffect(currentQuarter) {
        if (currentQuarter > 0 && currentQuarter <= 4) {
            secondsRemaining = 720
            while (secondsRemaining > 0) {
                delay(10)
                secondsRemaining--
            }
        }
    }

    LaunchedEffect(Unit) {
        if (currentQuarter == 0) {
            delay(1500)
            onAdvance()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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

            if (currentQuarter > 0) {
                GymClock(secondsRemaining)
            }

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
                                    delay(2500)
                                    onAdvance()
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
