import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.g5.di.appKoinModules
import com.g5.ui.BasketballDraftApp
import com.g5.ui.theme.AndroidIdeaTheme
import com.g5.ui.viewmodel.GameViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin

/** Équivalent web de MainActivity (Android) / BasketballDraftAppViewController (iOS) : démarre
 * Koin une fois puis monte l'app complète dans la page. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(appKoinModules()) }

    ComposeViewport {
        val viewModel: GameViewModel = koinViewModel()
        val uiState = viewModel.uiState.value

        AndroidIdeaTheme(darkTheme = uiState.isDarkTheme) {
            BasketballDraftApp(viewModel = viewModel)
        }
    }
}
