package com.example.androididea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androididea.ui.components.BasketballVisual
import com.example.androididea.ui.components.CourtLines
import com.example.androididea.ui.components.MenuButton
import com.example.androididea.ui.components.MenuButtonVariant
import com.example.androididea.ui.components.StatusBar
import com.example.androididea.viewmodel.Screen

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        StatusBar()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            CourtLines(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BasketballVisual(size = 112.dp)

                Text(
                    text = "BASKET",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "JEU DE SIMULATION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.4.sp,
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
                label = "JOUER CONTRE L'IA",
                sublabel = "Affronte l'ordinateur",
                onClick = { onNavigate(Screen.VsComputer) },
                variant = MenuButtonVariant.Primary
            )

            MenuButton(
                icon = Icons.Default.Group,
                label = "JOUER À DEUX",
                sublabel = "Défi joueur vs joueur",
                onClick = { onNavigate(Screen.VsHuman) },
                variant = MenuButtonVariant.Secondary
            )

            MenuButton(
                icon = Icons.Default.Settings,
                label = "OPTIONS",
                sublabel = "Réglages et préférences",
                onClick = { onNavigate(Screen.Options) },
                variant = MenuButtonVariant.Default
            )
        }

        Text(
            text = "v1.0 · Saison 2025–26",
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )
    }
}
