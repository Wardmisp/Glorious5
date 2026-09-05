package com.g5.ui.navigation

/** Destinations du graphe de navigation racine (voir [com.g5.MainActivity]). Le mode en ligne
 * (VsOnline) est une destination unique qui gère son propre sous-état interne
 * ([com.g5.ui.viewmodel.MultiplayerScreen]) : ses transitions sont pilotées par le serveur
 * (temps réel/polling), pas par une navigation utilisateur avant/arrière classique, donc elles
 * ne sont volontairement pas modélisées comme des entrées de back stack séparées. */
object Routes {
    const val Home = "home"
    const val VsComputer = "vs_computer"
    const val VsHuman = "vs_human"
    const val VsOnline = "vs_online"
    const val Options = "options"
    const val ScoutingReport = "scouting_report"
    const val Simulation = "simulation"
}
