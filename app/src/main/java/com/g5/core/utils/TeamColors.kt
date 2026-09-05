package com.g5.core.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object TeamColors {
    private val colors = mapOf(
        "ATL" to Color(0xFFE03A3E),
        "BOS" to Color(0xFF007A33),
        "BKN" to Color(0xFF000000),
        "CHA" to Color(0xFF1D1160),
        "CHI" to Color(0xFFCE1141),
        "CLE" to Color(0xFF860038),
        "DAL" to Color(0xFF00538C),
        "DEN" to Color(0xFF0E2240),
        "DET" to Color(0xFFC8102E),
        "GSW" to Color(0xFF1D428A),
        "HOU" to Color(0xFFCE1141),
        "IND" to Color(0xFF002D62),
        "LAC" to Color(0xFFC8102E),
        "LAL" to Color(0xFF552583),
        "MEM" to Color(0xFF5D76A9),
        "MIA" to Color(0xFF98002E),
        "MIL" to Color(0xFF00471B),
        "MIN" to Color(0xFF0C2340),
        "NOP" to Color(0xFF0C2340),
        "NYK" to Color(0xFF006BB6),
        "OKC" to Color(0xFF007AC1),
        "ORL" to Color(0xFF0077C0),
        "PHI" to Color(0xFF006BB6),
        "PHX" to Color(0xFF1D1160),
        "POR" to Color(0xFFE03A3E),
        "SAC" to Color(0xFF5A2D81),
        "SAS" to Color(0xFFC4CED4),
        "TOR" to Color(0xFFCE1141),
        "UTA" to Color(0xFF002B5C),
        "WAS" to Color(0xFF002B5C)
    )

    fun getColor(team: String): Color {
        return colors[team.uppercase()] ?: Color(0xFFF4722B) // Default Orange
    }

    fun getHexColor(team: String): String {
        val color = getColor(team)
        // Color.value est un ULong encodant potentiellement un espace colorimétrique large gamut ;
        // les octets ARGB "classiques" ne sont pas dans ses 32 bits de poids faible. toArgb() fait
        // la conversion correctement (avant, cette fonction retournait quasi toujours "#000000").
        return String.format("#%06X", 0xFFFFFF and color.toArgb())
    }
}
