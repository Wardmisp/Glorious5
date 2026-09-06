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
import org.jetbrains.compose.resources.stringResource
import com.g5.shared.resources.Res
import com.g5.shared.resources.common_options
import com.g5.shared.resources.home_options_sublabel
import com.g5.shared.resources.home_online_label
import com.g5.shared.resources.home_online_sublabel
import com.g5.shared.resources.home_split_screen_label
import com.g5.shared.resources.home_split_screen_sublabel
import com.g5.shared.resources.home_tagline
import com.g5.shared.resources.home_title
import com.g5.shared.resources.home_tutorial_label
import com.g5.shared.resources.home_tutorial_sublabel
import com.g5.shared.resources.home_version_footer
import com.g5.shared.resources.home_vs_computer_label
import com.g5.shared.resources.home_vs_computer_sublabel

@Composable
fun HomeScreen(
    versionName: String,
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
                    text = stringResource(Res.string.home_title),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = stringResource(Res.string.home_tagline),
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
                label = stringResource(Res.string.home_vs_computer_label),
                sublabel = stringResource(Res.string.home_vs_computer_sublabel),
                onClick = { onNavigate(Routes.VsComputer) },
                variant = MenuButtonVariant.Primary,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["home_ia"] = coords.boundsInRoot()
                }
            )

            MenuButton(
                icon = Icons.Default.Group,
                label = stringResource(Res.string.home_split_screen_label),
                sublabel = stringResource(Res.string.home_split_screen_sublabel),
                onClick = { onNavigate(Routes.VsHuman) },
                variant = MenuButtonVariant.Secondary,
                enabled = true
            )

            MenuButton(
                icon = Icons.Default.Public,
                label = stringResource(Res.string.home_online_label),
                sublabel = stringResource(Res.string.home_online_sublabel),
                onClick = { onNavigate(Routes.VsOnline) },
                variant = MenuButtonVariant.Secondary
            )

            MenuButton(
                icon = Icons.Default.School,
                label = stringResource(Res.string.home_tutorial_label),
                sublabel = stringResource(Res.string.home_tutorial_sublabel),
                onClick = onStartTutorial,
                variant = MenuButtonVariant.Secondary,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["home_tutorial"] = coords.boundsInRoot()
                }
            )

            MenuButton(
                icon = Icons.Default.Settings,
                label = stringResource(Res.string.common_options),
                sublabel = stringResource(Res.string.home_options_sublabel),
                onClick = { onNavigate(Routes.Options) },
                variant = MenuButtonVariant.Default
            )
        }

        Text(
            text = stringResource(Res.string.home_version_footer, versionName),
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
        )
    }
}
