package com.g5.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.g5.R

/** Traduit le code stable stocké dans NBAPlayer.position (voir formatPosition côté data) vers le
 * libellé affiché dans la langue courante. */
@Composable
fun positionLabel(code: String): String = stringResource(
    when (code) {
        "PG" -> R.string.position_pg
        "SG" -> R.string.position_sg
        "SF" -> R.string.position_sf
        "PF" -> R.string.position_pf
        "C" -> R.string.position_c
        "GF" -> R.string.position_gf
        "FC" -> R.string.position_fc
        else -> R.string.position_unknown
    }
)

/** Même code, sous forme abrégée (badges compacts du rapport de scouting). */
@Composable
fun positionAbbreviation(code: String): String = stringResource(
    when (code) {
        "PG" -> R.string.position_abbr_pg
        "SG" -> R.string.position_abbr_sg
        "SF" -> R.string.position_abbr_sf
        "PF" -> R.string.position_abbr_pf
        "C" -> R.string.position_abbr_c
        "GF" -> R.string.position_abbr_gf
        "FC" -> R.string.position_abbr_fc
        else -> R.string.position_abbr_unknown
    }
)
