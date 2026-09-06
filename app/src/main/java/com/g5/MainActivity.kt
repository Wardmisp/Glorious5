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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.g5.ui.components.TutorialOverlay
import com.g5.ui.components.TutorialStep
import com.g5.ui.navigation.NavCommand
import com.g5.ui.navigation.Routes
import com.g5.ui.screens.GameScreen
import com.g5.ui.screens.HomeScreen
import com.g5.ui.screens.MultiplayerLobbyScreen
import com.g5.ui.screens.MultiplayerMatchScreen
import com.g5.ui.screens.MultiplayerResultScreen
import com.g5.ui.screens.MultiplayerScoutingScreen
import com.g5.ui.screens.MultiplayerSimulationScreen
import com.g5.ui.screens.OptionsScreen
import com.g5.ui.screens.ScoutingReportScreen
import com.g5.ui.screens.SimulationScreen
import com.g5.ui.screens.SplitScreenGameScreen
import com.g5.ui.theme.AndroidIdeaTheme
import com.g5.ui.viewmodel.GameViewModel
import com.g5.ui.viewmodel.MultiplayerScreen
import com.g5.ui.viewmodel.MultiplayerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Log app launch event
        FirebaseAnalytics.getInstance(this).logEvent("app_launch", null)

        setContent {
            val viewModel: GameViewModel = koinViewModel()
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
    val tutorialPositions = remember { mutableStateMapOf<String, Rect>() }
    val navController = rememberNavController()

    // Navigations décidées côté ViewModel (fin de tutoriel, de manche, de simulation) — la
    // navigation purement mécanique (boutons retour/menu) est câblée directement sur le
    // NavController ci-dessous, sans passer par ce canal. Voir NavCommand.
    LaunchedEffect(viewModel) {
        viewModel.navCommands.collect { command ->
            when (command) {
                is NavCommand.NavigateTo -> navController.navigate(command.route) { launchSingleTop = true }
                is NavCommand.PopTo -> navController.popBackStack(command.route, command.inclusive)
            }
        }
    }

    val tutorialSteps = listOf(
        TutorialStep(stringResource(R.string.tutorial_step1_title), stringResource(R.string.tutorial_step1_body), "home_tutorial"),
        TutorialStep(stringResource(R.string.tutorial_step2_title), stringResource(R.string.tutorial_step2_body)),
        TutorialStep(stringResource(R.string.tutorial_step3_title), stringResource(R.string.tutorial_step3_body), "home_ia"),
        TutorialStep(stringResource(R.string.tutorial_step4_title), stringResource(R.string.tutorial_step4_body), "game_card"),
        TutorialStep(stringResource(R.string.tutorial_step5_title), stringResource(R.string.tutorial_step5_body), "game_timer"),
        TutorialStep(stringResource(R.string.tutorial_step6_title), stringResource(R.string.tutorial_step6_body), "game_bid"),
        TutorialStep(stringResource(R.string.tutorial_step7_title), stringResource(R.string.tutorial_step7_body), "game_pass"),
        TutorialStep(stringResource(R.string.tutorial_step8_title), stringResource(R.string.tutorial_step8_body), "game_teams"),
        TutorialStep(stringResource(R.string.tutorial_step9_title), stringResource(R.string.tutorial_step9_body)),
        TutorialStep(stringResource(R.string.tutorial_step10_title), stringResource(R.string.tutorial_step10_body), "scouting_win"),
        TutorialStep(stringResource(R.string.tutorial_step11_title), stringResource(R.string.tutorial_step11_body), "scouting_stats"),
        TutorialStep(stringResource(R.string.tutorial_step12_title), stringResource(R.string.tutorial_step12_body), "scouting_matchups"),
        TutorialStep(stringResource(R.string.tutorial_step13_title), stringResource(R.string.tutorial_step13_body))
    )

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
            NavHost(navController = navController, startDestination = Routes.Home) {
                composable(Routes.Home) {
                    HomeScreen(
                        onNavigate = { route ->
                            when (route) {
                                Routes.VsComputer -> viewModel.startGame(vsHuman = false)
                                Routes.VsHuman -> viewModel.startGame(vsHuman = true)
                                else -> navController.navigate(route)
                            }
                        },
                        onStartTutorial = { viewModel.startTutorial() },
                        tutorialPositions = tutorialPositions
                    )
                }
                composable(Routes.VsComputer) {
                    GameScreen(
                        vsComputer = true,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack(Routes.Home, false) },
                        tutorialPositions = tutorialPositions
                    )
                }
                composable(Routes.VsHuman) {
                    SplitScreenGameScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack(Routes.Home, false) }
                    )
                }
                composable(Routes.VsOnline) {
                    val multiplayerViewModel: MultiplayerViewModel = koinViewModel()
                    val mpState by multiplayerViewModel.uiState.collectAsState()

                    LaunchedEffect(Unit) {
                        multiplayerViewModel.enterLobby()
                    }

                    val onLeave: () -> Unit = {
                        multiplayerViewModel.leaveMatch()
                        navController.popBackStack(Routes.Home, false)
                    }

                    when (mpState.screen) {
                        is MultiplayerScreen.Lobby -> {
                            MultiplayerLobbyScreen(
                                state = mpState.lobby,
                                onBack = { navController.popBackStack(Routes.Home, false) },
                                onRefresh = { multiplayerViewModel.refreshOpenMatches() },
                                onCreateMatch = { multiplayerViewModel.createMatch() },
                                onJoinMatch = { matchId -> multiplayerViewModel.joinMatch(matchId) },
                                onJoinByCode = { multiplayerViewModel.joinByCode() },
                                onJoinCodeChange = { multiplayerViewModel.setJoinCodeInput(it) },
                                onBudgetChange = { multiplayerViewModel.setBudgetInput(it) }
                            )
                        }
                        is MultiplayerScreen.InMatch -> {
                            MultiplayerMatchScreen(
                                state = mpState.match,
                                onBack = onLeave,
                                onBidInputChange = { multiplayerViewModel.onBidInputChange(it) },
                                onPlaceBid = { multiplayerViewModel.placeBid() },
                                onPass = { multiplayerViewModel.pass() },
                                onTimerExpired = { multiplayerViewModel.handleTimeout() },
                                onDismissPendingResult = { multiplayerViewModel.dismissPendingResult() }
                            )
                        }
                        is MultiplayerScreen.Scouting -> {
                            MultiplayerScoutingScreen(
                                state = mpState.match,
                                onStartSimulation = { multiplayerViewModel.startSimulation() }
                            )
                        }
                        is MultiplayerScreen.Simulation -> {
                            MultiplayerSimulationScreen(
                                state = mpState.match,
                                onAdvance = { multiplayerViewModel.advanceSimulation() }
                            )
                        }
                        is MultiplayerScreen.Result -> {
                            MultiplayerResultScreen(
                                state = mpState.match,
                                onBack = onLeave
                            )
                        }
                    }
                }
                composable(Routes.Options) {
                    OptionsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.Simulation) {
                    SimulationScreen(
                        viewModel = viewModel
                    )
                }
                composable(Routes.ScoutingReport) {
                    val isVsHuman = uiState.gameState.isVsHuman
                    ScoutingReportScreen(
                        gameState = uiState.gameState,
                        onStartSimulation = { navController.navigate(Routes.Simulation) },
                        tutorialPositions = tutorialPositions,
                        labelA = if (isVsHuman) stringResource(R.string.common_player_number_upper, 1) else stringResource(R.string.scouting_default_label_you),
                        labelB = if (isVsHuman) stringResource(R.string.common_player_number_upper, 2) else stringResource(R.string.scouting_default_label_ai)
                    )
                }
            }
        }

        // Tutorial Overlay (Moved outside the scaled box to match boundsInRoot)
        if (uiState.isTutorialActive && uiState.tutorialStep < tutorialSteps.size) {
            val currentStep = tutorialSteps[uiState.tutorialStep]
            TutorialOverlay(
                step = currentStep,
                targetRect = currentStep.targetId?.let { tutorialPositions[it] },
                onNext = { viewModel.nextTutorialStep() },
                onSkip = { viewModel.skipTutorial() }
            )
        }
    }
}
