package com.example.androididea

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androididea.ui.screens.GameScreen
import com.example.androididea.ui.screens.HomeScreen
import com.example.androididea.ui.screens.OptionsScreen
import com.example.androididea.ui.theme.AndroidIdeaTheme
import com.example.androididea.viewmodel.GameViewModel
import com.example.androididea.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                            viewModel.navigateToScreen(screen)
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