package com.g5.ui.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.g5.shared.resources.*

/** Traduit le code stable stocké dans NBAPlayer.position (voir formatPosition côté data) vers le
 * libellé affiché dans la langue courante. */
@Composable
fun positionLabel(code: String): String = stringResource(
    when (code) {
        "PG" -> Res.string.position_pg
        "SG" -> Res.string.position_sg
        "SF" -> Res.string.position_sf
        "PF" -> Res.string.position_pf
        "C" -> Res.string.position_c
        "GF" -> Res.string.position_gf
        "FC" -> Res.string.position_fc
        else -> Res.string.position_unknown
    }
)

/** Même code, sous forme abrégée (badges compacts du rapport de scouting). */
@Composable
fun positionAbbreviation(code: String): String = stringResource(
    when (code) {
        "PG" -> Res.string.position_abbr_pg
        "SG" -> Res.string.position_abbr_sg
        "SF" -> Res.string.position_abbr_sf
        "PF" -> Res.string.position_abbr_pf
        "C" -> Res.string.position_abbr_c
        "GF" -> Res.string.position_abbr_gf
        "FC" -> Res.string.position_abbr_fc
        else -> Res.string.position_abbr_unknown
    }
)
