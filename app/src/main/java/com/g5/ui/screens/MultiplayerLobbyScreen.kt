package com.g5.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.R
import com.g5.data.remote.dto.Match
import com.g5.ui.viewmodel.LobbyUiState

@Composable
fun MultiplayerLobbyScreen(
    state: LobbyUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCreateMatch: () -> Unit,
    onJoinMatch: (String) -> Unit,
    onJoinByCode: () -> Unit,
    onJoinCodeChange: (String) -> Unit,
    onBudgetChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
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
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = stringResource(R.string.mp_lobby_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.mp_lobby_refresh),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.error?.let { message ->
                Text(
                    text = message,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFFE03A3E)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.mp_lobby_create_section),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Stepper(
                    label = stringResource(R.string.mp_lobby_budget_label),
                    value = "${state.budgetInput}$",
                    onDecrease = { onBudgetChange((state.budgetInput - 10).coerceAtLeast(10)) },
                    onIncrease = { onBudgetChange(state.budgetInput + 10) },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

            Button(
                onClick = onCreateMatch,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.mp_lobby_create_button),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.mp_lobby_join_section),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                OutlinedTextField(
                    value = state.joinCodeInput,
                    onValueChange = onJoinCodeChange,
                    placeholder = { Text(stringResource(R.string.mp_lobby_join_code_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = onJoinByCode,
                    enabled = !state.isLoading && state.joinCodeInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.common_join),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Text(
                text = stringResource(R.string.mp_lobby_open_matches),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            if (state.isLoading && state.openMatches.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF4722B))
                }
            } else if (state.openMatches.isEmpty()) {
                Text(
                    text = stringResource(R.string.mp_lobby_no_open_matches),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.openMatches.forEach { match ->
                        OpenMatchRow(match = match, onJoin = { onJoinMatch(match.id) })
                    }
                }
            }

            Box(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "–", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF4722B))
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF4722B))
            }
        }
    }
}

@Composable
private fun OpenMatchRow(match: Match, onJoin: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = Color(0xFFF4722B).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = Color(0xFFF4722B),
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.mp_lobby_match_summary, match.budget, match.teamSize),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.mp_lobby_waiting_for_opponent),
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Button(onClick = onJoin) {
            Text(
                text = stringResource(R.string.common_join),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
