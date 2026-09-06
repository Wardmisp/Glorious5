package com.g5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.ui.components.BasketballVisual
import com.g5.ui.components.CourtLines
import com.g5.ui.components.MenuButton
import com.g5.ui.components.MenuButtonVariant
import com.g5.ui.navigation.Routes

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot

/** Libellés affichés par [HomeScreen], résolus par l'appelant (ressources Android côté `app`,
 * chaînes en dur côté iOS) — l'écran de menu vit dans `shared` et ne peut pas dépendre de
 * `Context`/`R.string`, qui n'existent que sur Android. */
data class HomeScreenStrings(
    val title: String,
    val tagline: String,
    val vsComputerLabel: String,
    val vsComputerSublabel: String,
    val splitScreenLabel: String,
    val splitScreenSublabel: String,
    val onlineLabel: String,
    val onlineSublabel: String,
    val tutorialLabel: String,
    val tutorialSublabel: String,
    val optionsLabel: String,
    val optionsSublabel: String,
    /** Texte de pied de page déjà formaté avec le numéro de version (ex. "v0.0.5 · Saison 2025–26"). */
    val versionFooter: String
)

@Composable
fun HomeScreen(
    strings: HomeScreenStrings,
    onNavigate: (String) -> Unit,
    onStartTutorial: () -> Unit,
    tutorialPositions: MutableMap<String, Rect> = mutableMapOf(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            CourtLines(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BasketballVisual(size = 100.dp)

                Text(
                    text = strings.title,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = strings.tagline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.4.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFF4722B),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuButton(
                icon = Icons.Default.Devices,
                label = strings.vsComputerLabel,
                sublabel = strings.vsComputerSublabel,
                onClick = { onNavigate(Routes.VsComputer) },
                variant = MenuButtonVariant.Primary,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["home_ia"] = coords.boundsInRoot()
                }
            )

            MenuButton(
                icon = Icons.Default.Group,
                label = strings.splitScreenLabel,
                sublabel = strings.splitScreenSublabel,
                onClick = { onNavigate(Routes.VsHuman) },
                variant = MenuButtonVariant.Secondary,
                enabled = true
            )

            MenuButton(
                icon = Icons.Default.Public,
                label = strings.onlineLabel,
                sublabel = strings.onlineSublabel,
                onClick = { onNavigate(Routes.VsOnline) },
                variant = MenuButtonVariant.Secondary
            )

            MenuButton(
                icon = Icons.Default.School,
                label = strings.tutorialLabel,
                sublabel = strings.tutorialSublabel,
                onClick = onStartTutorial,
                variant = MenuButtonVariant.Secondary,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["home_tutorial"] = coords.boundsInRoot()
                }
            )

            MenuButton(
                icon = Icons.Default.Settings,
                label = strings.optionsLabel,
                sublabel = strings.optionsSublabel,
                onClick = { onNavigate(Routes.Options) },
                variant = MenuButtonVariant.Default
            )
        }

        Text(
            text = strings.versionFooter,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )
    }
}
