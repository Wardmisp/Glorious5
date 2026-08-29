package com.g5.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g5.data.repository.MultiplayerRepository
import com.g5.domain.model.TeamEntry
import com.g5.domain.usecase.CalculateWinProbabilityUseCase
import com.g5.domain.usecase.GenerateMatchSimulationUseCase
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

/** Le temps réel nécessite que les tables soient ajoutées à la publication supabase_realtime
 * côté Supabase ; ce polling est un filet de sécurité qui fait avancer le match même si ce
 * n'est pas (ou plus) le cas, au prix d'un délai au lieu d'une mise à jour instantanée. */
private const val FALLBACK_POLL_INTERVAL_MS = 4000L

private fun parseInstantMillis(iso: String): Long? =
    runCatching { OffsetDateTime.parse(iso).toInstant().toEpochMilli() }.getOrNull()

/**
 * Pilote le mode multijoueur en ligne (glorious5_multiplayer_schema.sql).
 * Séparé de [GameViewModel] : le modèle server-authoritative, tour par tour,
 * sans timer, n'a pas d'équivalent dans l'état du mode local (IA / hotseat).
 */
class MultiplayerViewModel : ViewModel() {
    private val repository = MultiplayerRepository()

    private val _uiState = MutableStateFlow(MultiplayerUiState())
    val uiState: StateFlow<MultiplayerUiState> = _uiState.asStateFlow()

    private var currentMatchId: String? = null
    private var matchChannel: RealtimeChannel? = null
    private var realtimeJob: Job? = null
    private var pollingJob: Job? = null

    private fun updateLobby(update: (LobbyUiState) -> LobbyUiState) {
        _uiState.update { it.copy(lobby = update(it.lobby)) }
    }

    private fun updateMatch(update: (MatchUiState) -> MatchUiState) {
        _uiState.update { it.copy(match = update(it.match)) }
    }

    /** Appelé à chaque entrée sur l'écran online. Sans effet si un match est déjà en cours. */
    fun enterLobby() {
        viewModelScope.launch {
            try {
                repository.ensureSignedIn()
            } catch (e: Exception) {
                updateLobby { it.copy(error = "Connexion impossible : ${e.message}") }
                return@launch
            }
            if (currentMatchId == null) {
                refreshOpenMatches()
            }
        }
    }

    fun refreshOpenMatches() {
        viewModelScope.launch {
            updateLobby { it.copy(isLoading = true, error = null) }
            try {
                val matches = repository.listOpenMatches()
                updateLobby { it.copy(isLoading = false, openMatches = matches) }
            } catch (e: Exception) {
                updateLobby { it.copy(isLoading = false, error = "Impossible de charger les parties : ${e.message}") }
            }
        }
    }

    fun setBudgetInput(value: Int) = updateLobby { it.copy(budgetInput = value) }
    fun setJoinCodeInput(value: String) = updateLobby { it.copy(joinCodeInput = value) }

    /** Crée un match en attente : visible dans le lobby public ET partageable via son id (le "code").
     * La taille d'équipe n'est pas configurable côté client : toujours 5. */
    fun createMatch() {
        viewModelScope.launch {
            updateLobby { it.copy(isLoading = true, error = null) }
            try {
                val lobby = _uiState.value.lobby
                val matchId = repository.createMatch(
                    opponentId = null,
                    budget = lobby.budgetInput,
                    teamSize = 5
                )
                updateLobby { it.copy(isLoading = false) }
                enterMatch(matchId)
            } catch (e: Exception) {
                updateLobby { it.copy(isLoading = false, error = "Création impossible : ${e.message}") }
            }
        }
    }

    fun joinMatch(matchId: String) {
        viewModelScope.launch {
            updateLobby { it.copy(isLoading = true, error = null) }
            try {
                val myId = repository.currentUserId()
                val match = repository.getMatch(matchId)
                if (match.status == "waiting" && match.player1Id != myId) {
                    repository.joinMatch(matchId)
                }
                updateLobby { it.copy(isLoading = false) }
                enterMatch(matchId)
            } catch (e: Exception) {
                updateLobby { it.copy(isLoading = false, error = "Impossible de rejoindre : ${e.message}") }
            }
        }
    }

    fun joinByCode() {
        val code = _uiState.value.lobby.joinCodeInput.trim()
        if (code.isNotEmpty()) joinMatch(code)
    }

    private fun enterMatch(matchId: String) {
        currentMatchId = matchId
        _uiState.update {
            it.copy(
                screen = MultiplayerScreen.InMatch,
                match = MatchUiState(myUserId = repository.currentUserId().orEmpty())
            )
        }
        startObserving(matchId)
    }

    private fun startObserving(matchId: String) {
        realtimeJob?.cancel()
        pollingJob?.cancel()
        viewModelScope.launch {
            // 0. Synchronise l'horloge serveur avant tout calcul de chrono (voir syncClock) :
            // sans ça, un appareil dont l'heure système est décalée afficherait un compte à
            // rebours faux en continu, pas juste en retard.
            repository.syncClock()

            // 1. Hydratation classique d'abord (donne aussi les ids d'équipe nécessaires aux filtres realtime)
            val teamIds = reloadMatchState(matchId)

            // 2. Canal + flows enregistrés avant le seul subscribe()
            val channel = repository.openMatchChannel(matchId)
            matchChannel = channel
            val matchFlow = repository.matchChanges(channel, matchId)
            val auctionFlow = repository.auctionChanges(channel, matchId)
            val rosterFlow = if (teamIds.isNotEmpty()) repository.rosterChanges(channel, teamIds) else emptyFlow()

            realtimeJob = launch {
                merge(matchFlow, auctionFlow, rosterFlow).collect {
                    reloadMatchState(matchId)
                }
            }

            try {
                repository.subscribeChannel(channel)
                updateMatch { it.copy(isRealtimeConnected = true) }
            } catch (e: Exception) {
                updateMatch { it.copy(isRealtimeConnected = false) }
            }
        }

        // Filet de sécurité : fait avancer le match même si le temps réel ne délivre rien
        // (table absente de la publication supabase_realtime, websocket qui tombe, etc.).
        // S'arrête de lui-même via stopObserving() dès que le match est "completed".
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(FALLBACK_POLL_INTERVAL_MS)
                reloadMatchState(matchId)
            }
        }
    }

    /**
     * Recharge l'état complet depuis Supabase (source de vérité). Retourne les ids d'équipe du
     * match.
     *
     * L'enchère la plus récente du match (quel que soit son type) pilote l'affichage :
     * - 'active' → c'est l'enchère en cours, affichée normalement.
     * - 'completed' et pas encore vue → tampon "joueur remporté" (mise conclue, passe, timeout,
     *   OU attribution automatique — les deux cas sont traités pareil, unifiés ici) tant que
     *   l'utilisateur ne l'a pas fermé (dismissPendingResult).
     * Le prochain joueur n'est présenté (par le créateur du match uniquement, cf. plan) qu'une
     * fois qu'il n'y a plus ni enchère active ni tampon à acquitter — jamais en cascade silencieuse.
     */
    private suspend fun reloadMatchState(matchId: String): List<String> {
        val myId = repository.currentUserId() ?: return emptyList()
        return try {
            val match = repository.getMatch(matchId)
            val teams = repository.getMatchTeams(matchId)
            val myTeam = teams.find { it.userId == myId }
            val opponentTeam = teams.find { it.userId != myId }
            val teamIds = teams.map { it.id }

            val rosterRows = repository.getRosters(teamIds)
            val latestAuction = repository.getLatestAuction(matchId)
            val activeAuction = latestAuction?.takeIf { it.status == "active" }

            val playerIds = (rosterRows.map { it.nbaPlayerId } + listOfNotNull(latestAuction?.nbaPlayerId)).distinct()
            val players = repository.getNbaPlayers(playerIds).associateBy { it.id }

            val myRoster = rosterRows.filter { it.matchTeamId == myTeam?.id }
                .mapNotNull { row -> players[row.nbaPlayerId]?.let { TeamEntry(it, row.pricePaid) } }
            val opponentRoster = rosterRows.filter { it.matchTeamId == opponentTeam?.id }
                .mapNotNull { row -> players[row.nbaPlayerId]?.let { TeamEntry(it, row.pricePaid) } }

            val currentPlayer = activeAuction?.let { players[it.nbaPlayerId] }
            val bidCount = activeAuction?.let { repository.getBids(it.id) }?.count { it.amount != null } ?: 0
            val turnDeadlineAtMillis = activeAuction?.turnDeadline?.let { parseInstantMillis(it) }
                ?.let { repository.toLocalMillis(it) }
            val isMyTurn = activeAuction?.turnUserId == myId

            val previousState = _uiState.value.match
            val pendingResult: CompletedAuctionInfo? = when {
                // Déjà affiché : on ne le remplace pas tant que l'utilisateur ne l'a pas fermé.
                previousState.pendingResult != null -> previousState.pendingResult
                latestAuction != null && latestAuction.status == "completed" &&
                    latestAuction.id != previousState.lastDismissedAuctionId -> {
                    val winnerId = latestAuction.winnerId
                    val player = players[latestAuction.nbaPlayerId]
                    if (winnerId != null && player != null) {
                        CompletedAuctionInfo(
                            auctionId = latestAuction.id,
                            player = player,
                            winnerIsMe = winnerId == myId,
                            pricePaid = latestAuction.finalPrice ?: 0,
                            isAutoAssigned = latestAuction.auctionType == "auto_assign",
                            isLastPick = myRoster.size >= match.teamSize && opponentRoster.size >= match.teamSize
                        )
                    } else null
                }
                else -> null
            }

            _uiState.update {
                val minValidBid = maxOf(1, (activeAuction?.currentBid ?: 0) + 1)
                val isSameAuction = activeAuction != null && activeAuction.id == it.match.currentAuction?.id
                // Ne réinitialise l'input que si nécessaire (nouvelle enchère, ou la valeur en
                // cours n'est plus valide) — sinon un rechargement en arrière-plan (polling,
                // realtime) effacerait ce que le joueur vient de saisir avant même qu'il valide.
                val nextBidInput = if (isSameAuction && it.match.bidInput >= minValidBid) {
                    it.match.bidInput
                } else {
                    minValidBid
                }

                it.copy(
                    match = it.match.copy(
                        myUserId = myId,
                        match = match,
                        myTeam = myTeam,
                        opponentTeam = opponentTeam,
                        myRoster = myRoster,
                        opponentRoster = opponentRoster,
                        currentAuction = activeAuction,
                        currentPlayer = currentPlayer,
                        bidCount = bidCount,
                        turnDeadlineAtMillis = turnDeadlineAtMillis,
                        isMyTurn = isMyTurn,
                        bidInput = nextBidInput,
                        pendingResult = pendingResult,
                        error = null
                    )
                )
            }

            if (match.status == "completed") {
                // Le résultat est déjà décidé côté serveur (compute_match_result). Si le dernier
                // pick est encore affiché en tampon, on attend que l'utilisateur le ferme
                // (dismissPendingResult) avant de passer au rapport de scouting.
                if (pendingResult == null) {
                    finalizeCompletedMatch(myRoster, opponentRoster)
                }
            } else {
                _uiState.update { it.copy(screen = MultiplayerScreen.InMatch) }

                // Seul le créateur du match (player1) déclenche la présentation du joueur
                // suivant, pour éviter que les deux clients créent chacun une enchère active en
                // même temps (present_next_player() ne le protège pas lui-même, cf. plan) — et
                // seulement quand il n'y a plus rien à acquitter, jamais en cascade silencieuse.
                if (match.status == "drafting" && activeAuction == null && pendingResult == null && match.player1Id == myId) {
                    presentNextPlayerOnce(matchId)
                } else if (activeAuction != null && activeAuction.turnDeadline == null &&
                    activeAuction.currentBidderId == null && activeAuction.turnUserId == myId
                ) {
                    // Le chrono d'ouverture ne démarre que lorsque le joueur dont c'est le tour
                    // de miser est effectivement présent et prêt (voir start_turn_clock) — pas
                    // dès la création de l'enchère côté serveur, qui peut survenir avant que son
                    // écran n'ait fini de charger. Une fois posé, turn_deadline est un timestamp
                    // serveur partagé : l'adversaire le voit défiler à l'identique dès son
                    // prochain rechargement (temps réel ou polling), sans action de sa part.
                    startTurnClockOnce(matchId, activeAuction.id)
                }
            }

            teamIds
        } catch (e: Exception) {
            updateMatch { it.copy(error = "Synchronisation impossible : ${e.message}") }
            emptyList()
        }
    }

    /** Ferme l'écran "tampon" affiché après une enchère résolue ou une attribution automatique. */
    fun dismissPendingResult() {
        val matchId = currentMatchId ?: return
        val dismissedAuctionId = _uiState.value.match.pendingResult?.auctionId
        updateMatch {
            it.copy(
                pendingResult = null,
                lastDismissedAuctionId = dismissedAuctionId ?: it.lastDismissedAuctionId
            )
        }
        // Réévalue tout de suite (plutôt que d'attendre le prochain polling) : soit un autre
        // tampon attend déjà (cascade d'attributions), soit c'est au créateur du match de
        // présenter la suite, soit le match est en réalité terminé.
        viewModelScope.launch { reloadMatchState(matchId) }
    }

    /**
     * Calculé une seule fois : un nouvel appel après coup (poll en vol, etc.) ne réécrase rien.
     * Volontairement non-suspend : [stopObserving] peut annuler le job (realtime ou polling) qui
     * est justement en train d'exécuter cet appel, donc le travail réseau se fait dans une
     * coroutine détachée de viewModelScope plutôt que de continuer dans l'appelant (qui vient
     * potentiellement de s'auto-annuler et se ferait interrompre au prochain point de suspension).
     */
    private fun finalizeCompletedMatch(myRoster: List<TeamEntry>, opponentRoster: List<TeamEntry>) {
        if (_uiState.value.match.analytics != null) return
        stopObserving()
        viewModelScope.launch {
            try {
                val allPlayers = repository.getAllNbaPlayers()
                val analytics = CalculateWinProbabilityUseCase().execute(
                    teamA = myRoster.map { it.player },
                    teamB = opponentRoster.map { it.player },
                    allSeasons = allPlayers
                )
                val simulation = GenerateMatchSimulationUseCase().execute(
                    teamA = myRoster.map { it.player },
                    teamB = opponentRoster.map { it.player },
                    winProbA = analytics.first.winProbability
                )
                _uiState.update {
                    it.copy(
                        screen = MultiplayerScreen.Scouting,
                        match = it.match.copy(
                            analytics = analytics,
                            matchSimulation = simulation,
                            currentSimulationQuarter = 0
                        )
                    )
                }
            } catch (e: Exception) {
                // Pas de rapport possible : on va directement au résultat, déjà connu côté serveur.
                _uiState.update { it.copy(screen = MultiplayerScreen.Result) }
            }
        }
    }

    fun startSimulation() {
        _uiState.update { it.copy(screen = MultiplayerScreen.Simulation) }
    }

    fun advanceSimulation() {
        val current = _uiState.value.match.currentSimulationQuarter
        if (current < 4) {
            updateMatch { it.copy(currentSimulationQuarter = current + 1) }
        } else {
            _uiState.update { it.copy(screen = MultiplayerScreen.Result) }
        }
    }

    private fun stopObserving() {
        realtimeJob?.cancel()
        realtimeJob = null
        pollingJob?.cancel()
        pollingJob = null
        val channel = matchChannel
        matchChannel = null
        if (channel != null) {
            viewModelScope.launch { repository.closeChannel(channel) }
        }
    }

    /**
     * Présente le joueur suivant — une seule étape, jamais en boucle silencieuse : que le
     * résultat soit une enchère active ou une attribution automatique déjà conclue, le
     * rechargement qui suit se charge de l'afficher (comme tampon dans le second cas) et
     * d'attendre l'acquittement de l'utilisateur avant d'aller plus loin.
     */
    private suspend fun presentNextPlayerOnce(matchId: String) {
        repository.presentNextPlayer(matchId)
        reloadMatchState(matchId)
    }

    /** Démarre le chrono d'ouverture — appelé par l'ouvreur lui-même dès qu'il voit l'enchère. */
    private suspend fun startTurnClockOnce(matchId: String, auctionId: String) {
        repository.startTurnClock(auctionId)
        reloadMatchState(matchId)
    }

    fun onBidInputChange(value: Int) = updateMatch { it.copy(bidInput = value) }

    fun placeBid() {
        val matchId = currentMatchId ?: return
        val state = _uiState.value.match
        val auction = state.currentAuction ?: return
        val myTeam = state.myTeam ?: return
        if (!state.isMyTurn || state.isSubmittingBid) return
        val amount = state.bidInput
        if (amount <= auction.currentBid || amount > myTeam.budgetRemaining) {
            updateMatch { it.copy(error = "Mise invalide") }
            return
        }
        viewModelScope.launch {
            updateMatch { it.copy(isSubmittingBid = true, error = null) }
            try {
                repository.placeBid(auction.id, state.myUserId, amount)
            } catch (e: Exception) {
                updateMatch { it.copy(error = "Mise refusée, resynchronisation...") }
            }
            updateMatch { it.copy(isSubmittingBid = false) }
            reloadMatchState(matchId)
        }
    }

    fun pass() {
        val matchId = currentMatchId ?: return
        val state = _uiState.value.match
        val auction = state.currentAuction ?: return
        if (!state.isMyTurn || state.isSubmittingBid || !state.canPass) return
        viewModelScope.launch {
            updateMatch { it.copy(isSubmittingBid = true, error = null) }
            try {
                repository.passBid(auction.id, state.myUserId)
            } catch (e: Exception) {
                updateMatch { it.copy(error = "Action refusée, resynchronisation...") }
            }
            updateMatch { it.copy(isSubmittingBid = false) }
            reloadMatchState(matchId)
        }
    }

    /**
     * Appelé côté client quand le chrono de 15s arrive à zéro — par n'importe lequel des deux
     * joueurs, que ce soit son propre tour ou non. La RPC expire_turn_if_overdue() est sans
     * danger à appeler à l'aveugle : elle vérifie elle-même côté serveur que le délai est
     * réellement dépassé avant d'agir, donc un appel spéculatif ou en double ne fait rien.
     * C'est ce qui permet de débloquer un tour même si l'appareil du joueur en retard est hors
     * ligne : l'adversaire peut forcer la résolution depuis le sien.
     */
    fun handleTimeout() {
        val matchId = currentMatchId ?: return
        val auctionId = _uiState.value.match.currentAuction?.id ?: return
        viewModelScope.launch {
            try {
                repository.expireTurnIfOverdue(auctionId)
            } catch (e: Exception) {
                // best-effort : le prochain rechargement (polling/realtime) resynchronisera
            }
            reloadMatchState(matchId)
        }
    }

    fun leaveMatch() {
        stopObserving()
        currentMatchId = null
        _uiState.update { it.copy(screen = MultiplayerScreen.Lobby, match = MatchUiState()) }
        refreshOpenMatches()
    }

    override fun onCleared() {
        stopObserving()
    }
}
