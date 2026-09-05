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
        TutorialStep("Bienvenue !", "Découvrez comment bâtir votre équipe de légende dans Glorious 5.", "home_tutorial"),
        TutorialStep("Le Marché", "Le but est de recruter 5 joueurs. Mais attention, votre budget est limité à 50$ !"),
        TutorialStep("Commencer", "Commençons par lancer une partie contre l'ordinateur.", "home_ia"),
        TutorialStep("La Carte Joueur", "Voici le joueur mis en vente. Ses statistiques sont cachées au début et se révèlent au fil des enchères.", "game_card"),
        TutorialStep("Le Chronomètre", "Chaque tour dure 15 secondes. Si personne ne mise avant la fin, le dernier enchéreur emporte le joueur.", "game_timer"),
        TutorialStep("Miser", "Utilisez ces boutons pour augmenter l'enchère. Soyez stratégique pour ne pas vider votre budget trop vite !", "game_bid"),
        TutorialStep("Passer", "Si le prix est trop élevé ou si le joueur ne vous intéresse pas, vous pouvez passer.", "game_pass"),
        TutorialStep("Votre Équipe", "Consultez l'état de votre effectif et votre argent restant à tout moment ici.", "game_teams"),
        TutorialStep("Analyse d'avant-match", "Une fois l'équipe bâtie, le Scouting Report analyse vos chances de victoire."),
        TutorialStep("Probabilités", "Découvrez vos chances de succès basées sur l'équilibre et le talent de votre effectif.", "scouting_win"),
        TutorialStep("Forces & Faiblesses", "Comparez les secteurs de jeu : Attaque, Défense, Playmaking...", "scouting_stats"),
        TutorialStep("Duels Clés", "Voyez qui domine à chaque poste. Les noms en gras indiquent un avantage statistique.", "scouting_matchups"),
        TutorialStep("C'est parti !", "Vous savez tout ! Bonne chance pour monter la meilleure équipe possible.")
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
                        labelA = if (isVsHuman) "JOUEUR 1" else "VOUS",
                        labelB = if (isVsHuman) "JOUEUR 2" else "IA"
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
