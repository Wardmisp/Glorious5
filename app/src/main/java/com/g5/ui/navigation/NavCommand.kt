package com.g5.ui.navigation

/**
 * Intention de navigation émise par un ViewModel suite à une décision métier (fin de tutoriel,
 * fin de manche, fin de simulation...) — la navigation "mécanique" déclenchée par un simple clic
 * utilisateur (bouton retour, menu) est câblée directement sur le [androidx.navigation.NavController]
 * dans MainActivity, sans passer par ce canal.
 */
sealed class NavCommand {
    data class NavigateTo(val route: String) : NavCommand()
    data class PopTo(val route: String, val inclusive: Boolean = false) : NavCommand()
}
