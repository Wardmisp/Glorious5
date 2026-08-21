package com.g5.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.g5.data.local.BUDGET
import com.g5.data.local.NBA_PLAYERS
import com.g5.data.local.TOTAL
import com.g5.domain.model.TeamEntry
import com.g5.data.repository.PlayerRepository
import com.g5.domain.usecase.CalculateWinProbabilityUseCase
import com.g5.domain.usecase.GenerateMatchSimulationUseCase
import com.g5.core.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val playerRepository = PlayerRepository(application)
    private val _uiState = mutableStateOf<UiState>(UiState(gameState = GameState(players = NBA_PLAYERS.take(TOTAL))))
    val uiState: State<UiState> = _uiState
    private val soundManager = SoundManager(application).apply {
        isEnabled = _uiState.value.isSoundEnabled
    }
    private var timerJob: Job? = null

    init {
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

    fun navigateToScreen(screen: Screen, reset: Boolean = false) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentScreen = screen)
        
        // Arrêt systématique du timer si on quitte l'écran de jeu
        if (screen != Screen.VsComputer && screen != Screen.VsHuman) {
            timerJob?.cancel()
            soundManager.stopSound()
        }
        
        if (reset && (screen == Screen.VsComputer || screen == Screen.VsHuman)) {
            resetGameState()
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
            tutorialStep = 0,
            currentScreen = Screen.Home
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
            3 -> navigateToScreen(Screen.VsComputer, reset = true)
            9 -> setupTutorialScoutingState()
            13 -> skipTutorial()
        }
    }

    private fun setupTutorialScoutingState() {
        viewModelScope.launch {
            val allPlayers = playerRepository.getAllSeasons().ifEmpty { NBA_PLAYERS }
            
            // L'IA reçoit le gratin (les 5 meilleurs)
            val aiPlayers = allPlayers.take(5)
            
            // L'utilisateur reçoit des joueurs nettement moins forts (le bas du classement)
            // Dans le top 300, les derniers sont d'excellents joueurs mais bien moins "historiques"
            val userPlayers = if (allPlayers.size > 10) allPlayers.takeLast(5) else allPlayers.drop(5).take(5)

            val useCase = CalculateWinProbabilityUseCase()
            val results = useCase.execute(
                teamA = userPlayers,
                teamB = aiPlayers,
                allSeasons = allPlayers
            )

            val simUseCase = GenerateMatchSimulationUseCase()
            val simulation = simUseCase.execute(
                teamA = userPlayers,
                teamB = aiPlayers,
                winProbA = results.first.winProbability
            )

            updateGameState { it.copy(
                teams = Pair(
                    userPlayers.map { p -> TeamEntry(p, 5) },
                    aiPlayers.map { p -> TeamEntry(p, 45) }
                ),
                analytics = results,
                luckyWinner = 2,
                matchSimulation = simulation
            ) }
            
            navigateToScreen(Screen.ScoutingReport)
        }
    }

    fun skipTutorial() {
        timerJob?.cancel() // Arrêt du timer si on quitte le tuto
        soundManager.stopSound()
        _uiState.value = _uiState.value.copy(
            isTutorialActive = false,
            isFirstLaunch = false
        )
        navigateToScreen(Screen.Home)
    }

    private fun resetGameState() {
        timerJob?.cancel()
        viewModelScope.launch {
            val players = playerRepository.getAuctionPlayers(TOTAL)
            val difficulty = _uiState.value.difficulty
            
            val (playerBudget, aiBudget) = when (difficulty) {
                Difficulty.BEGINNER -> Pair(BUDGET + 10, BUDGET)
                Difficulty.NORMAL -> Pair(BUDGET, BUDGET)
                Difficulty.DIFFICULT -> Pair(BUDGET, BUDGET + 10)
            }

            updateGameState { 
                GameState(
                    budgets = Pair(playerBudget, aiBudget),
                    teams = Pair(emptyList(), emptyList()),
                    revealOrder = generateRevealOrder(),
                    players = players,
                    luckyWinner = null
                )
            }
            startTimer()
            soundManager.playBeginAuction()
        }
    }

    private fun generateRevealOrder(): List<Int> {
        val baseWeights = listOf(
            100, // 0: PTS
            55,  // 1: REB
            75,  // 2: AST
            30,  // 3: STL
            10,  // 4: BLK
            5,   // 5: Season
            20,  // 6: Position
            150, // 7: Team
            200, // 8: FirstName
            250  // 9: LastName
        )
        return (0..9).map { index ->
            index to (baseWeights[index] + ((-10..10).random()))
        }.sortedBy { it.second }.map { it.first }
    }

    fun setBid(amount: Int, bidder: Int) {
        updateGameState { currentState ->
            currentState.copy(
                bid = amount,
                bidder = bidder,
                bidCount = currentState.bidCount + 1
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        soundManager.stopSound()
        timerJob = viewModelScope.launch {
            var seconds = 15
            while (seconds >= 0) {
                if (!_uiState.value.isTutorialActive) {
                    updateGameState { it.copy(timer = seconds) }
                    if (seconds in 1..5) {
                        soundManager.playAlarmAuction()
                    }
                    if (seconds == 0) {
                        val currentState = _uiState.value.gameState
                        if (currentState.bid > 0) {
                            adjudicate(currentState.bid, currentState.bidder)
                        } else {
                            pass()
                        }
                        break
                    }
                    seconds--
                }
                delay(1000)
            }
        }
    }

    fun setP1Input(value: Int) {
        updateGameState { it.copy(p1Input = value) }
    }

    fun setP2Input(value: Int) {
        updateGameState { it.copy(p2Input = value) }
    }

    fun handleP1Bid(minBid: Int, budget: Int) {
        val currentState = _uiState.value.gameState
        if (currentState.teams.first.size >= TOTAL / 2) return
        
        val amount = maxOf(currentState.p1Input, minBid)
        if (amount <= budget) {
            setBid(amount, 1)
            setP1Input(amount + 1)
        }
    }

    fun handleP2Bid(minBid: Int, budget: Int) {
        val currentState = _uiState.value.gameState
        if (currentState.teams.second.size >= TOTAL / 2) return

        val amount = maxOf(currentState.p2Input, minBid)
        if (amount <= budget) {
            setBid(amount, 2)
            setP2Input(amount + 1)
        }
    }

    fun computerBid(minBid: Int) {
        val currentState = _uiState.value.gameState
        val p1Full = currentState.teams.first.size >= TOTAL / 2
        val p2Full = currentState.teams.second.size >= TOTAL / 2

        if (currentState.bidder == 2 || currentState.done || p2Full) return

        // Si l'adversaire est plein, l'ordi DOIT récupérer le joueur
        if (p1Full) {
            // On le récupère gratuitement si on n'a pas encore misé
            val finalBid = if (currentState.bid > 0) currentState.bid else 0
            adjudicate(finalBid, 2)
            return
        }

        val player = currentState.players.getOrNull(currentState.round)
            ?: NBA_PLAYERS.getOrNull(currentState.round)
            ?: return
        
        val baseValuation = (
            player.pts * 0.8 + 
            player.reb * 0.5 + 
            player.ast * 0.6 + 
            player.stl * 1.5 + 
            player.blk * 1.5
        )
        
        val seed = currentState.round + (currentState.budgets.second / 10)
        val randomFactor = (0.8 + (Math.abs(seed.hashCode() % 40) / 100.0))
        
        val personalValuation = (baseValuation * randomFactor).toInt()
        
        val budgetLimit = if (personalValuation > 25) 
            currentState.budgets.second 
        else 
            (currentState.budgets.second * 0.6).toInt()
            
        val maxBid = minOf(personalValuation, budgetLimit)
        
        if (currentState.bid >= maxBid) return

        updateGameState { it.copy(thinking = true) }
        viewModelScope.launch {
            val baseDelay = if (currentState.bid == 0) 2400L else 1200L
            val extraDelay = (1000..2600).random().toLong()
            
            delay(baseDelay + extraDelay)
            
            val updatedState = _uiState.value.gameState
            if (!updatedState.done && updatedState.bidder != 2 && updatedState.bid < maxBid) {
                val budgetRatio = updatedState.budgets.second.toFloat() / BUDGET
                val interestRatio = personalValuation.toFloat() / 25f 
                
                val maxJump = when {
                    interestRatio > 0.9f && budgetRatio > 0.7f -> 5
                    interestRatio > 0.7f && budgetRatio > 0.4f -> 3
                    interestRatio > 0.5f -> 2
                    else -> 1
                }
                
                val jump = (1..maxJump).random()
                val nextBid = minOf(updatedState.bid + jump, maxBid)
                
                setBid(nextBid, 2)
            }
            updateGameState { it.copy(thinking = false) }
        }
    }

    fun adjudicate(bid: Int, bidder: Int?) {
        if (bidder == null) return
        
        timerJob?.cancel()
        
        updateGameState { currentState ->
            val player = currentState.players.getOrNull(currentState.round)
                ?: NBA_PLAYERS.getOrNull(currentState.round)
                ?: return@updateGameState currentState
            
            val newBudgets = if (bidder == 1) {
                Pair(currentState.budgets.first - bid, currentState.budgets.second)
            } else {
                Pair(currentState.budgets.first, currentState.budgets.second - bid)
            }

            val newTeams = if (bidder == 1) {
                Pair(
                    currentState.teams.first + TeamEntry(player, bid),
                    currentState.teams.second
                )
            } else {
                Pair(
                    currentState.teams.first,
                    currentState.teams.second + TeamEntry(player, bid)
                )
            }

            currentState.copy(
                bid = bid,
                budgets = newBudgets,
                teams = newTeams,
                awardedTo = bidder,
                done = true,
                thinking = false
            )
        }
        soundManager.playWinAuction()
    }

    fun nextRound() {
        timerJob?.cancel()
        
        val currentState = _uiState.value.gameState
        val totalPlayers = if (currentState.players.isNotEmpty()) currentState.players.size else TOTAL
        val nextRoundIndex = currentState.round + 1

        if (nextRoundIndex >= totalPlayers) {
            viewModelScope.launch {
                val allSeasons = playerRepository.getAllSeasons()
                val useCase = CalculateWinProbabilityUseCase()
                val results = useCase.execute(
                    teamA = currentState.teams.first.map { it.player },
                    teamB = currentState.teams.second.map { it.player },
                    allSeasons = allSeasons
                )
                
                // Tirage au sort basé sur les pourcentages
                val p1WinProb = results.first.winProbability
                val randomValue = Math.random()
                val winner = if (randomValue < p1WinProb) 1 else 2
                
                // Génération de la simulation
                val simUseCase = GenerateMatchSimulationUseCase()
                val simulation = simUseCase.execute(
                    teamA = currentState.teams.first.map { it.player },
                    teamB = currentState.teams.second.map { it.player },
                    winProbA = p1WinProb
                )

                updateGameState { it.copy(
                    analytics = results, 
                    luckyWinner = winner,
                    matchSimulation = simulation,
                    currentSimulationQuarter = 0
                ) }
                
                navigateToScreen(Screen.ScoutingReport)
            }
        } else {
            updateGameState { state ->
                GameState(
                    round = nextRoundIndex,
                    bid = 0,
                    bidder = null,
                    p1Input = 1,
                    p2Input = 1,
                    budgets = state.budgets,
                    teams = state.teams,
                    bidCount = 0,
                    revealOrder = generateRevealOrder(),
                    timer = 15,
                    players = state.players
                )
            }

            val p1Full = currentState.teams.first.size >= TOTAL / 2
            val p2Full = currentState.teams.second.size >= TOTAL / 2

            if (p1Full || p2Full) {
                // Attribution automatique
                val winner = if (p1Full) 2 else 1
                adjudicate(0, winner)
            } else {
                startTimer()
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
            updateGameState { it.copy(gameOver = true) }
            navigateToScreen(Screen.VsComputer, reset = false) // On revient sur l'écran de jeu SANS reset
            val winner = _uiState.value.gameState.luckyWinner
            soundManager.playResultScreen(isWinner = winner == 1)
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

    fun pass() {
        val currentState = _uiState.value.gameState
        val p1Full = currentState.teams.first.size >= TOTAL / 2
        val p2Full = currentState.teams.second.size >= TOTAL / 2

        if (!p2Full) {
            // L'adversaire a de la place, il récupère le joueur
            // S'il menait déjà, il garde son prix, sinon prix actuel ou 0 (si p1Full)
            val finalBid = if (currentState.bidder == 2) {
                currentState.bid 
            } else {
                if (p1Full) 0 else maxOf(1, currentState.bid)
            }
            adjudicate(finalBid, 2)
        } else if (!p1Full) {
            // L'adversaire est plein mais j'ai de la place, je récupère le joueur
            val finalBid = if (currentState.bidder == 1) {
                currentState.bid
            } else {
                if (p2Full) 0 else maxOf(1, currentState.bid)
            }
            adjudicate(finalBid, 1)
        } else {
            // Les deux sont pleins (ne devrait pas arriver avec TOTAL=10), on skip
            timerJob?.cancel()
            soundManager.stopSound()
            updateGameState { it.copy(done = true, awardedTo = null, thinking = false) }
        }
    }

    override fun onCleared() {
        // soundManager.release() // Optional: depends on lifecycle
    }

    fun goBack() {
        navigateToScreen(Screen.Home)
    }
}
