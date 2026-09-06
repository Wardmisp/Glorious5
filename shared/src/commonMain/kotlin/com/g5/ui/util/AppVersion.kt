package com.g5.ui.util

import androidx.compose.runtime.Composable

/** Numéro de version affiché en pied de page du menu (voir HomeScreen) — résolu différemment par
 * plateforme (PackageManager sur Android, NSBundle sur iOS). */
@Composable
expect fun appVersionName(): String
