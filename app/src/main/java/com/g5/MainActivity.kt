package com.g5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.compose.viewmodel.koinViewModel
import com.g5.ui.BasketballDraftApp
import com.g5.ui.theme.AndroidIdeaTheme
import com.g5.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Log app launch event
        FirebaseAnalytics.getInstance(this).logEvent("app_launch", null)

        setContent {
            val viewModel: GameViewModel = koinViewModel()
            val uiState = viewModel.uiState.value

            AndroidIdeaTheme(darkTheme = uiState.isDarkTheme) {
                BasketballDraftApp(viewModel = viewModel)
            }
        }
    }
}
