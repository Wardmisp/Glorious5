package com.g5.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g5.core.utils.SoundManager
import com.g5.data.local.NBA_PLAYERS
import com.g5.domain.model.BUDGET
import com.g5.domain.model.GameState
import com.g5.domain.model.NBAPlayer
import com.g5.domain.model.TOTAL
import com.g5.domain.repository.PlayerRepository
import com.g5.domain.usecase.AuctionUseCase
import com.g5.domain.usecase.BuildTutorialDemoUseCase
import com.g5.domain.usecase.CalculateWinProbabilityUseCase
import com.g5.domain.usecase.ComputerBidUseCase
import com.g5.domain.usecase.GenerateMatchSimulationUseCase
import com.g5.ui.navigation.NavCommand
import com.g5.ui.navigation.Routes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val playerRepository: PlayerRepository,
    private val soundManager: SoundManager,
    private val auctionUseCase: AuctionUseCase,
    private val computerBidUseCase: ComputerBidUseCase,
    private val calculateWinProbabilityUseCase: CalculateWinProbabilityUseCase,
    private val generateMatchSimulationUseCase: GenerateMatchSimulationUseCase,
    private val buildTutorialDemoUseCase: BuildTutorialDemoUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf<UiState>(UiState(gameState = GameState(players = NBA_PLAYERS.take(TOTAL))))
    val uiState: State<UiState> = _uiState

    /** Navigations décidées par une logique métier (fin de tutoriel, de manche, de simulation) —
     * la navigation purement mécanique (boutons retour/menu) est câblée directement sur le
     * NavController dans MainActivity, voir [com.g5.ui.navigation.NavCommand]. */
    private val _navCommands = Channel<NavCommand>(Channel.BUFFERED)
    val navCommands: Flow<NavCommand> = _navCommands.receiveAsFlow()

    init {
        soundManager.isEnabled = _uiState.value.isSoundEnabled

        viewModelScope.launch {
            val initialPlayers = playerRepository.getAuctionPlayers(TOTAL)
            if (initialPlayers.isNotEmpty()) {
                updateGameState { it.copy(players = initialPlayers) }
            }
        }

        // Auto-start tutorial highlight on first launch
        if (_uiState.value.isFirstLaunch) {
            _uiState.value = _uiState.value.copy(
                isTutorialActive = true,
                tutorialStep = 0
            )
        }
    }

    private fun updateGameState(update: (GameState) -> GameState) {
        _uiState.value = _uiState.value.copy(
            gameState = update(_uiState.value.gameState)
        )
    }

    /** Résout le joueur du round courant, avec le pool statique en dernier recours si jamais
     * l'état n'en a pas encore reçu (ex. tout premier rendu, avant la réponse du repository). */
    private fun currentPlayerOrFallback(state: GameState): NBAPlayer? =
        state.players.getOrNull(state.round) ?: NBA_PLAYERS.getOrNull(state.round)

    /** Arrête tout son de manche en cours — à appeler quand l'utilisateur quitte l'écran de jeu
     * en pleine enchère (bouton retour), pour ne pas laisser sonner l'alarme sur l'écran suivant. */
    fun onExitGameScreen() {
        soundManager.stopSound()
    }

    /** Démarre une nouvelle partie (IA ou hotseat) et navigue vers l'écran de jeu — utilisé aussi
     * bien par le menu d'accueil que par l'étape 3 du tutoriel. */
    fun startGame(vsHuman: Boolean) {
        viewModelScope.launch {
            val players = playerRepository.getAuctionPlayers(TOTAL)
            val difficulty = _uiState.value.difficulty

            val (playerBudget, aiBudget) = if (vsHuman) {
                Pair(BUDGET, BUDGET)
            } else {
                when (difficulty) {
                    Difficulty.BEGINNER -> Pair(BUDGET + 10, BUDGET)
                    Difficulty.NORMAL -> Pair(BUDGET, BUDGET)
                    Difficulty.DIFFICULT -> Pair(BUDGET, BUDGET + 10)
                }
            }

            updateGameState {
                GameState(
                    budgets = Pair(playerBudget, aiBudget),
                    teams = Pair(emptyList(), emptyList()),
                    revealOrder = auctionUseCase.generateRevealOrder(),
                    players = players,
                    luckyWinner = null,
                    activePlayerTurn = 1,
                    isVsHuman = vsHuman
                )
            }
            soundManager.playBeginAuction()
            _navCommands.send(NavCommand.NavigateTo(if (vsHuman) Routes.VsHuman else Routes.VsComputer))
        }
    }

    fun toggleTheme() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isDarkTheme = !currentState.isDarkTheme)
    }

    fun toggleSound() {
        val currentState = _uiState.value
        val newState = !currentState.isSoundEnabled
        _uiState.value = currentState.copy(isSoundEnabled = newState)
        soundManager.isEnabled = newState
    }

    fun setDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(difficulty = difficulty)
    }

    fun startTutorial() {
        _uiState.value = _uiState.value.copy(
            isTutorialActive = true,
            isFirstLaunch = false,
            tutorialStep = 0
        )
    }

    fun nextTutorialStep() {
        val currentStep = _uiState.value.tutorialStep
        val nextStep = currentStep + 1
        _uiState.value = _uiState.value.copy(
            tutorialStep = nextStep,
            isFirstLaunch = false
        )

        // Navigation logic for tutorial
        when (nextStep) {
            3 -> startGame(vsHuman = false)
            9 -> setupTutorialScoutingState()
            13 -> skipTutorial()
        }
    }

    private fun setupTutorialScoutingState() {
        viewModelScope.launch {
            val allPlayers = playerRepository.getAllSeasons().ifEmpty { NBA_PLAYERS }
            val demo = buildTutorialDemoUseCase.execute(allPlayers)

            updateGameState {
                it.copy(
                    teams = demo.teams,
                    analytics = demo.analytics,
                    luckyWinner = 2,
                    matchSimulation = demo.matchSimulation
                )
            }

            _navCommands.send(NavCommand.NavigateTo(Routes.ScoutingReport))
        }
    }

    fun skipTutorial() {
        soundManager.stopSound()
        _uiState.value = _uiState.value.copy(
            isTutorialActive = false,
            isFirstLaunch = false
        )
        viewModelScope.launch { _navCommands.send(NavCommand.PopTo(Routes.Home)) }
    }

    fun setBid(amount: Int, bidder: Int) {
        updateGameState { currentState ->
            currentState.copy(
                bid = amount,
                bidder = bidder,
                bidCount = currentState.bidCount + 1
            )
        }
    }

    fun updateTimer(seconds: Int) {
        updateGameState { it.copy(timer = seconds) }
    }

    fun onTimerExpired() {
        val currentState = _uiState.value.gameState
        if (currentState.bid > 0) {
            adjudicate(currentState.bid, currentState.bidder)
        } else {
            pass()
        }
    }

    fun playAlarmSound() {
        soundManager.playAlarmAuction()
    }

    fun setP1Input(value: Int) {
        updateGameState { it.copy(p1Input = value) }
    }

    fun setP2Input(value: Int) {
        updateGameState { it.copy(p2Input = value) }
    }

    fun handleP1Bid(minBid: Int, budget: Int) {
        val currentState = _uiState.value.gameState
        if (auctionUseCase.isTeamFull(currentState, 1)) return

        val amount = maxOf(currentState.p1Input, minBid)
        if (amount <= budget) {
            setBid(amount, 1)
            setP1Input(amount + 1)
            setP2Input(amount + 1)
            updateGameState { it.copy(activePlayerTurn = 2) }
        }
    }

    fun handleP2Bid(minBid: Int, budget: Int) {
        val currentState = _uiState.value.gameState
        if (auctionUseCase.isTeamFull(currentState, 2)) return

        val amount = maxOf(currentState.p2Input, minBid)
        if (amount <= budget) {
            setBid(amount, 2)
            setP2Input(amount + 1)
            setP1Input(amount + 1)
            updateGameState { it.copy(activePlayerTurn = 1) }
        }
    }

    fun passP1() = pass(passedBy = 1)
    fun passP2() = pass(passedBy = 2)

    fun toggleP1Team() {
        updateGameState { it.copy(showP1Team = !it.showP1Team) }
    }

    fun toggleP2Team() {
        updateGameState { it.copy(showP2Team = !it.showP2Team) }
    }

    suspend fun computerBid() {
        val currentState = _uiState.value.gameState
        if (currentState.bidder == 2 || currentState.done || auctionUseCase.isTeamFull(currentState, 2)) return

        // Si l'adversaire est plein, l'ordi DOIT récupérer le joueur (gratuitement s'il n'a pas
        // encore misé).
        if (auctionUseCase.isTeamFull(currentState, 1)) {
            adjudicate(if (currentState.bid > 0) currentState.bid else 0, 2)
            return
        }

        val player = currentPlayerOrFallback(currentState) ?: return
        val valuation = computerBidUseCase.evaluate(
            player = player,
            round = currentState.round,
            aiBudget = currentState.budgets.second,
            currentBid = currentState.bid
        )

        if (!valuation.canBid) {
            updateGameState { it.copy(thinking = true) }
            delay(computerBidUseCase.passDelayMillis())
            val updatedState = _uiState.value.gameState
            if (!updatedState.done && updatedState.bidder != 2) {
                pass(passedBy = 2)
            }
            updateGameState { it.copy(thinking = false) }
            return
        }

        updateGameState { it.copy(thinking = true) }
        delay(computerBidUseCase.thinkingDelayMillis(currentState.bid))

        val updatedState = _uiState.value.gameState
        if (!updatedState.done && updatedState.bidder != 2) {
            if (updatedState.bid < valuation.maxBid && updatedState.budgets.second > updatedState.bid) {
                val nextBid = computerBidUseCase.nextBidAmount(valuation, updatedState.bid, updatedState.budgets.second)
                setBid(nextBid, 2)
            } else {
                pass(passedBy = 2)
            }
        }
        updateGameState { it.copy(thinking = false) }
    }

    fun adjudicate(bid: Int, bidder: Int?) {
        if (bidder == null) return
        val currentState = _uiState.value.gameState
        val player = currentPlayerOrFallback(currentState) ?: return
        updateGameState { auctionUseCase.adjudicate(it, player, bid, bidder) }
        soundManager.playWinAuction()
    }

    fun nextRound() {
        val currentState = _uiState.value.gameState
        val totalPlayers = if (currentState.players.isNotEmpty()) currentState.players.size else TOTAL
        val nextRoundIndex = currentState.round + 1

        if (nextRoundIndex >= totalPlayers) {
            viewModelScope.launch {
                val allSeasons = playerRepository.getAllSeasons()
                val results = calculateWinProbabilityUseCase.execute(
                    teamA = currentState.teams.first.map { it.player },
                    teamB = currentState.teams.second.map { it.player },
                    allSeasons = allSeasons
                )

                // Tirage au sort basé sur les pourcentages
                val p1WinProb = results.first.winProbability
                val winner = if (Math.random() < p1WinProb) 1 else 2

                val simulation = generateMatchSimulationUseCase.execute(
                    teamA = currentState.teams.first.map { it.player },
                    teamB = currentState.teams.second.map { it.player },
                    winProbA = p1WinProb
                )

                updateGameState {
                    it.copy(
                        analytics = results,
                        luckyWinner = winner,
                        matchSimulation = simulation,
                        currentSimulationQuarter = 0
                    )
                }

                _navCommands.send(NavCommand.NavigateTo(Routes.ScoutingReport))
            }
        } else {
            updateGameState { auctionUseCase.startRound(it, nextRoundIndex) }

            val p1Full = auctionUseCase.isTeamFull(currentState, 1)
            val p2Full = auctionUseCase.isTeamFull(currentState, 2)

            if (p1Full || p2Full) {
                // Attribution automatique
                val winner = if (p1Full) 2 else 1
                adjudicate(0, winner)
            } else {
                soundManager.playBeginAuction()
            }
        }
    }

    fun advanceSimulation() {
        val currentState = _uiState.value.gameState
        if (currentState.currentSimulationQuarter < 4) {
            updateGameState { it.copy(currentSimulationQuarter = it.currentSimulationQuarter + 1) }
            playActionBeginSound()
        } else {
            val isVsHuman = currentState.isVsHuman
            updateGameState { it.copy(gameOver = true) }
            val winner = currentState.luckyWinner
            soundManager.playResultScreen(isWinner = winner == 1)
            // On revient sur l'écran de jeu (déjà dans la back stack) SANS le recréer : c'est lui
            // qui affiche l'écran de fin de partie une fois gameState.gameOver à true.
            viewModelScope.launch {
                _navCommands.send(NavCommand.PopTo(if (isVsHuman) Routes.VsHuman else Routes.VsComputer))
            }
        }
    }

    fun toggleTeamsPanel() {
        updateGameState { it.copy(showTeams = !it.showTeams) }
    }

    fun playActionBuzzer() {
        soundManager.playActionBuzzer()
    }

    fun playActionBeginSound() {
        soundManager.playActionBegin()
    }

    fun pass(passedBy: Int = 1) {
        val currentState = _uiState.value.gameState
        if (currentState.done) return

        val outcome = auctionUseCase.resolvePass(currentState, passedBy)
        if (outcome != null) {
            adjudicate(outcome.finalBid, outcome.awardedTo)
        } else {
            // Les deux camps sont pleins (ne devrait pas arriver avec TOTAL=10), on skip.
            soundManager.stopSound()
            updateGameState { it.copy(done = true, awardedTo = null, thinking = false) }
        }
    }

    override fun onCleared() {
        soundManager.stopSound()
    }
}
