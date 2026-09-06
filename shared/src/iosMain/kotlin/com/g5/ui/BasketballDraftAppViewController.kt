package com.g5.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.g5.di.ensureKoinStarted
import com.g5.ui.theme.AndroidIdeaTheme
import com.g5.ui.viewmodel.GameViewModel
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIViewController

/** Point d'entrée appelé depuis Swift (voir `iosApp/iosApp/ContentView.swift`) pour héberger
 * l'app complète (menu, IA, split-screen, en ligne, tutoriel...) dans un `UIViewController`
 * classique — équivalent iOS de MainActivity côté Android. */
fun BasketballDraftAppViewController(): UIViewController {
    ensureKoinStarted()
    return ComposeUIViewController {
        val viewModel: GameViewModel = koinViewModel()
        val uiState = viewModel.uiState.value

        AndroidIdeaTheme(darkTheme = uiState.isDarkTheme) {
            BasketballDraftApp(viewModel = viewModel)
        }
    }
}
