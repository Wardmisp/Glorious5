package com.example.androididea.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androididea.ui.components.StatusBar
import com.example.androididea.ui.viewmodel.GameViewModel

@Composable
fun OptionsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.value
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        StatusBar()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "OPTIONS",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (uiState.isDarkTheme) Color(0xFFBB86FC).copy(alpha = 0.1f) 
                                        else Color(0xFFF4722B).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (uiState.isDarkTheme) Color(0xFFBB86FC) else Color(0xFFF4722B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Mode Sombre",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isDarkTheme) "Activé" else "Désactivé",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Switch(
                    checked = uiState.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFF4722B),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            Text(
                text = "D'autres réglages (volume, difficulté) arriveront bientôt dans cette section.",
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
    }
}
