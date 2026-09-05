package com.g5.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.R
import com.g5.domain.model.NBAPlayer
import com.g5.ui.util.positionLabel

@Composable
fun PlayerRevealCard(
    player: NBAPlayer,
    bidCount: Int,
    revealOrder: List<Int>,
    revealed: Boolean,
    modifier: Modifier = Modifier
) {
    // Mapping des index définis dans GameViewModel
    val idxPts = 0
    val idxReb = 1
    val idxAst = 2
    val idxStl = 3
    val idxBlk = 4
    val idxSeason = 5
    val idxPosition = 6
    val idxTeam = 7
    val idxFirstName = 8
    val idxLastName = 9

    fun isRevealed(index: Int): Boolean {
        if (revealed) return true
        val threshold = revealOrder.indexOf(index) + 1
        return bidCount >= threshold
    }

    val stats = listOf(
        Stat(stringResource(R.string.stat_pts), player.pts, isRevealed(idxPts)),
        Stat(stringResource(R.string.stat_reb), player.reb, isRevealed(idxReb)),
        Stat(stringResource(R.string.stat_ast), player.ast, isRevealed(idxAst)),
        Stat(stringResource(R.string.stat_stl), player.stl, isRevealed(idxStl)),
        Stat(stringResource(R.string.stat_blk), player.blk, isRevealed(idxBlk))
    )

    val teamColorInt = try {
        android.graphics.Color.parseColor(player.teamColor)
    } catch (e: Exception) {
        android.graphics.Color.parseColor("#0E2240")
    }
    val teamColor = Color(teamColorInt)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0x1C1C1C),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Team color header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = teamColor.copy(alpha = 0.88f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRevealed(idxTeam)) player.team else stringResource(R.string.common_mystery_team),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.95f)
                )
                Text(
                    text = if (isRevealed(idxSeason)) player.season else "????-??",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Position badge
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFF4722B).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF4722B).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isRevealed(idxPosition)) positionLabel(player.position) else "??",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFFF4722B)
                )
            }

            // Player name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val showFirst = isRevealed(idxFirstName)
                val showLast = isRevealed(idxLastName)

                if (showFirst || showLast) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (showFirst) player.displayFirstName else "???",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = if (showLast) player.displayLastName.uppercase() else "????????",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFFF4722B),
                            lineHeight = 28.sp
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .size(width = 80.dp, height = 14.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .size(width = 112.dp, height = 14.dp)
                            )
                        }
                    }
                }
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stats.forEach { stat ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (stat.revealed) {
                            StatBox(stat)
                        } else {
                            EmptyStatBox()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(stat: Stat) {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = String.format("%.1f", stat.value),
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFFF4722B)
        )
        Text(
            text = stat.label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun EmptyStatBox() {
    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "—",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Text(
            text = "???",
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

data class Stat(val label: String, val value: Double, val revealed: Boolean)
