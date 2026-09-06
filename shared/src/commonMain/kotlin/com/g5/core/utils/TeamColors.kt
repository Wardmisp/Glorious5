package com.g5.core.utils

object TeamColors {
    // Couleurs ARGB (0xAARRGGBB) stockées en Int brut plutôt qu'en androidx.compose.ui.graphics.Color :
    // ce module doit rester consommable depuis un commonMain futur, où Compose n'existe pas.
    private val colors = mapOf(
        "ATL" to 0xFFE03A3E.toInt(),
        "BOS" to 0xFF007A33.toInt(),
        "BKN" to 0xFF000000.toInt(),
        "CHA" to 0xFF1D1160.toInt(),
        "CHI" to 0xFFCE1141.toInt(),
        "CLE" to 0xFF860038.toInt(),
        "DAL" to 0xFF00538C.toInt(),
        "DEN" to 0xFF0E2240.toInt(),
        "DET" to 0xFFC8102E.toInt(),
        "GSW" to 0xFF1D428A.toInt(),
        "HOU" to 0xFFCE1141.toInt(),
        "IND" to 0xFF002D62.toInt(),
        "LAC" to 0xFFC8102E.toInt(),
        "LAL" to 0xFF552583.toInt(),
        "MEM" to 0xFF5D76A9.toInt(),
        "MIA" to 0xFF98002E.toInt(),
        "MIL" to 0xFF00471B.toInt(),
        "MIN" to 0xFF0C2340.toInt(),
        "NOP" to 0xFF0C2340.toInt(),
        "NYK" to 0xFF006BB6.toInt(),
        "OKC" to 0xFF007AC1.toInt(),
        "ORL" to 0xFF0077C0.toInt(),
        "PHI" to 0xFF006BB6.toInt(),
        "PHX" to 0xFF1D1160.toInt(),
        "POR" to 0xFFE03A3E.toInt(),
        "SAC" to 0xFF5A2D81.toInt(),
        "SAS" to 0xFFC4CED4.toInt(),
        "TOR" to 0xFFCE1141.toInt(),
        "UTA" to 0xFF002B5C.toInt(),
        "WAS" to 0xFF002B5C.toInt()
    )

    private const val DEFAULT_ARGB = 0xFFF4722B.toInt() // Default Orange

    fun getArgb(team: String): Int = colors[team.uppercase()] ?: DEFAULT_ARGB

    fun getHexColor(team: String): String {
        val rgb = 0xFFFFFF and getArgb(team)
        return "#" + rgb.toString(16).uppercase().padStart(6, '0')
    }
}
