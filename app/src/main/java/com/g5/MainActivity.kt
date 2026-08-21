package com.g5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.g5.ui.screens.GameScreen
import com.g5.ui.screens.HomeScreen
import com.g5.ui.screens.OptionsScreen
import com.g5.ui.screens.ScoutingReportScreen
import com.g5.ui.screens.SimulationScreen
import com.g5.ui.theme.AndroidIdeaTheme
import com.g5.ui.viewmodel.GameViewModel
import com.g5.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Log app launch event
        FirebaseAnalytics.getInstance(this).logEvent("app_launch", null)

        setContent {
            val viewModel: GameViewModel = viewModel()
            val uiState = viewModel.uiState.value

            AndroidIdeaTheme (darkTheme = uiState.isDarkTheme) {
                BasketballDraftApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BasketballDraftApp(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val uiState = viewModel.uiState.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Phone frame simulation (optional, can be removed for full screen)
        Box(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        44.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onNavigate = { screen ->
                            viewModel.navigateToScreen(screen, reset = true)
                        }
                    )
                }
                is Screen.VsComputer -> {
                    GameScreen(
                        vsComputer = true,
                        viewModel = viewModel,
                        onBack = {
                            viewModel.navigateToScreen(Screen.Home)
                        }
                    )
                }
                is Screen.VsHuman -> {
                    GameScreen(
                        vsComputer = false,
                        viewModel = viewModel,
                        onBack = {
                            viewModel.navigateToScreen(Screen.Home)
                        }
                    )
                }
                is Screen.Options -> {
                    OptionsScreen(
                        viewModel = viewModel,
                        onBack = {
                            viewModel.navigateToScreen(Screen.Home)
                        }
                    )
                }
                is Screen.Simulation -> {
                    SimulationScreen(
                        viewModel = viewModel
                    )
                }
                is Screen.ScoutingReport -> {
                    ScoutingReportScreen(
                        gameState = uiState.gameState,
                        onStartSimulation = {
                            viewModel.navigateToScreen(Screen.Simulation)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun BasketballDraftAppPreview() {
    AndroidIdeaTheme {
        val viewModel: GameViewModel = viewModel()
        BasketballDraftApp(viewModel = viewModel)
    }
}