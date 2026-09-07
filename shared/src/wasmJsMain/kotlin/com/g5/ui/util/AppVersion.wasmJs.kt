package com.g5.ui.util

import androidx.compose.runtime.Composable

// Pas de manifeste/Info.plist côté web pour lire un numéro de version : gardé en dur, à tenir à
// jour avec app/build.gradle.kts (versionName) au besoin.
@Composable
actual fun appVersionName(): String = "0.0.5-alpha"
