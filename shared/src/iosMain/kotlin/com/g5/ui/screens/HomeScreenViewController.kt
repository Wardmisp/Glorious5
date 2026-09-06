package com.g5.ui.screens

import androidx.compose.ui.window.ComposeUIViewController
import com.g5.ui.theme.AndroidIdeaTheme
import platform.UIKit.UIViewController

/** Point d'entrée appelé depuis Swift (voir `iosApp/iosApp/ContentView.swift`) pour héberger
 * l'écran de menu Compose Multiplatform dans un `UIViewController` classique. */
fun HomeScreenViewController(
    versionName: String,
    onNavigate: (String) -> Unit,
    onStartTutorial: () -> Unit
): UIViewController = ComposeUIViewController {
    AndroidIdeaTheme {
        HomeScreen(
            versionName = versionName,
            onNavigate = onNavigate,
            onStartTutorial = onStartTutorial
        )
    }
}
