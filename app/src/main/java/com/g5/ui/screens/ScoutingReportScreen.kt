package com.g5.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.domain.model.PlayerScore
import com.g5.domain.model.TeamAnalytics
import com.g5.ui.components.MenuButton
import com.g5.ui.components.MenuButtonVariant
import com.g5.ui.components.StatusBar
import com.g5.ui.viewmodel.GameState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect

@Composable
fun ScoutingReportScreen(
    gameState: GameState,
    onStartSimulation: () -> Unit,
    tutorialPositions: MutableMap<String, Rect> = mutableMapOf(),
    modifier: Modifier = Modifier
) {
    val analytics = gameState.analytics ?: return
    var viewMode by remember { mutableStateOf(0) } // 0: Bars, 1: Radar

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StatusBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RAPPORT DE SCOUTING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFFF4722B)
                )
                Text(
                    text = "ANALYSE D'AVANT-MATCH",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Win Probability Visual
            WinProbabilityCard(
                analytics.first, 
                analytics.second,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["scouting_win"] = coords.boundsInRoot()
                }
            )

            // Team Comparisons Header with Toggle
            Row(
                modifier = Modifier.fillMaxWidth().onGloballyPositioned { coords ->
                    tutorialPositions["scouting_stats"] = coords.boundsInRoot()
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPARAISON DES ÉQUIPES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp
                )
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == 0) Color(0xFFF4722B) else Color.Transparent)
                            .clickable { viewMode = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (viewMode == 1) Color(0xFFF4722B) else Color.Transparent)
                            .clickable { viewMode = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            if (viewMode == 0) {
                TeamComparisonStats(analytics.first, analytics.second)
            } else {
                TeamRadarComparison(analytics.first, analytics.second)
            }

            // Matchup Advantages (Sorted by position rank for better comparison)
            val sortedA = analytics.first.scoredPlayers.sortedBy { getPositionRank(it.player.position) }
            val sortedB = analytics.second.scoredPlayers.sortedBy { getPositionRank(it.player.position) }
            MatchupAdvantages(
                sortedA, 
                sortedB,
                modifier = Modifier.onGloballyPositioned { coords ->
                    tutorialPositions["scouting_matchups"] = coords.boundsInRoot()
                }
            )

            // X-Factors
            XFactors(analytics.first, analytics.second)

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            MenuButton(
                icon = Icons.Default.PlayArrow,
                label = "LANCER LA SIMULATION",
                sublabel = "Début du coup d'envoi",
                onClick = onStartSimulation,
                variant = MenuButtonVariant.Primary
            )
        }
    }
}

private fun getPositionRank(position: String): Int {
    return when (position) {
        "Meneur" -> 1
        "Arrière" -> 2
        "Arrière-Ailier" -> 3
        "Ailier" -> 4
        "Ailier Fort" -> 5
        "Intérieur" -> 6
        "Pivot" -> 7
        else -> 8
    }
}

@Composable
fun WinProbabilityCard(teamA: TeamAnalytics, teamB: TeamAnalytics, modifier: Modifier = Modifier) {
    val probA = (teamA.winProbability * 100).roundToInt()
    val probB = (teamB.winProbability * 100).roundToInt()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PROBABILITÉ DE VICTOIRE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$probA%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = if (probA >= probB) Color(0xFFF4722B) else Color.White
                )
                Text(text = "VOUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .padding(horizontal = 24.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(teamA.winProbability.toFloat())
                            .background(Brush.horizontalGradient(listOf(Color(0xFFF4722B), Color(0xFFF59E0B))))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(teamB.winProbability.toFloat())
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$probB%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = if (probB > probA) Color(0xFFF4722B) else Color.White
                )
                Text(text = "IA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun TeamComparisonStats(teamA: TeamAnalytics, teamB: TeamAnalytics) {
    val statsA = calculateTeamStats(teamA.scoredPlayers)
    val statsB = calculateTeamStats(teamB.scoredPlayers)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ComparisonRow("Attaque (Shoot)", statsA.shooting, statsB.shooting)
        ComparisonRow("Défense (Paint)", statsA.defense, statsB.defense)
        ComparisonRow("Playmaking", statsA.playmaking, statsB.playmaking)
        ComparisonRow("Dominance (Impact)", statsA.dominance, statsB.dominance)
    }
}

@Composable
fun TeamRadarComparison(teamA: TeamAnalytics, teamB: TeamAnalytics) {
    val statsA = calculateTeamStats(teamA.scoredPlayers)
    val statsB = calculateTeamStats(teamB.scoredPlayers)

    val labels = listOf("Attaque", "Défense", "Playmaking", "Dominance")
    val valuesA = listOf(statsA.shooting, statsA.defense, statsA.playmaking, statsA.dominance)
    val valuesB = listOf(statsB.shooting, statsB.defense, statsB.playmaking, statsB.dominance)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        RadarChart(
            labels = labels,
            teamAValues = valuesA,
            teamBValues = valuesB,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun RadarChart(
    labels: List<String>,
    teamAValues: List<Double>,
    teamBValues: List<Double>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val center = Offset(w / 2f, h / 2f)
        val radius = minOf(w, h) / 2f * 0.65f
        val numAxes = labels.size
        val angleStep = 2 * PI / numAxes

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw background circles/polygons
            for (i in 1..4) {
                val r = radius * (i / 4f)
                val path = Path()
                for (j in 0 until numAxes) {
                    val angle = j * angleStep - PI / 2
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()
                    if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, Color.White.copy(alpha = 0.05f), style = Stroke(width = 1.dp.toPx()))
            }

            // Draw axes
            for (i in 0 until numAxes) {
                val angle = i * angleStep - PI / 2
                val x = center.x + radius * cos(angle).toFloat()
                val y = center.y + radius * sin(angle).toFloat()
                drawLine(Color.White.copy(alpha = 0.1f), center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // Draw Team B (IA) polygon
            val pathB = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - PI / 2
                val r = radius * teamBValues[i].toFloat()
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (i == 0) pathB.moveTo(x, y) else pathB.lineTo(x, y)
            }
            pathB.close()
            drawPath(pathB, Color.White.copy(alpha = 0.2f), style = Fill)
            drawPath(pathB, Color.White.copy(alpha = 0.4f), style = Stroke(width = 2.dp.toPx()))

            // Draw Team A (VOUS) polygon
            val pathA = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - PI / 2
                val r = radius * teamAValues[i].toFloat()
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (i == 0) pathA.moveTo(x, y) else pathA.lineTo(x, y)
            }
            pathA.close()
            drawPath(pathA, Color(0xFFF4722B).copy(alpha = 0.3f), style = Fill)
            drawPath(pathA, Color(0xFFF4722B), style = Stroke(width = 3.dp.toPx()))
        }

        // Position labels manually
        labels.forEachIndexed { i, label ->
            val angle = i * angleStep - PI / 2
            val labelRadius = radius + 24.dp.value * density.density
            val x = center.x + labelRadius * cos(angle).toFloat()
            val y = center.y + labelRadius * sin(angle).toFloat()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(
                        x = with(density) { (x - w / 2).toDp() },
                        y = with(density) { (y - h / 2).toDp() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ComparisonRow(label: String, valA: Double, valB: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
            Text(
                text = "${(valA * 100).roundToInt()} vs ${(valB * 100).roundToInt()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth(valA.toFloat())
                        .clip(CircleShape)
                        .background(if (valA >= valB) Color(0xFFF4722B) else Color.White.copy(alpha = 0.3f))
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth(valB.toFloat())
                        .clip(CircleShape)
                        .background(if (valB > valA) Color(0xFFF4722B) else Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun MatchupAdvantages(playersA: List<PlayerScore>, playersB: List<PlayerScore>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AVANTAGES PAR POSITION",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp
        )

        playersA.forEachIndexed { index, scoreA ->
            val scoreB = playersB.getOrNull(index)
            if (scoreB != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAWinner = scoreA.totalScore >= scoreB.totalScore
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scoreA.player.lastName,
                            fontSize = 14.sp,
                            fontWeight = if (isAWinner) FontWeight.Black else FontWeight.Normal,
                            color = if (isAWinner) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "%.1f".format(scoreA.totalScore),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAWinner) Color(0xFFF4722B) else Color.White.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = scoreA.player.season,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.width(60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = scoreA.player.position.take(3).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF4722B)
                        )
                        Text(
                            text = "VS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                        Text(
                            text = scoreB.player.position.take(3).uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF4722B)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = scoreB.player.lastName,
                            textAlign = TextAlign.End,
                            fontSize = 14.sp,
                            fontWeight = if (!isAWinner) FontWeight.Black else FontWeight.Normal,
                            color = if (!isAWinner) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = scoreB.player.season,
                                textAlign = TextAlign.End,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "%.1f".format(scoreB.totalScore),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isAWinner) Color(0xFFF4722B) else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
                
                if (index < playersA.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
fun XFactors(teamA: TeamAnalytics, teamB: TeamAnalytics) {
    val statsA = calculateTeamStats(teamA.scoredPlayers)
    val statsB = calculateTeamStats(teamB.scoredPlayers)
    
    val factors = mutableListOf<String>()
    
    if (statsA.defense > statsB.defense + 0.1) {
        factors.add("Votre équipe domine la raquette en défense.")
    } else if (statsB.defense > statsA.defense + 0.1) {
        factors.add("L'IA possède un rempart défensif supérieur.")
    }
    
    if (statsA.shooting > statsB.shooting + 0.05) {
        factors.add("Avantage net au tir extérieur pour vous.")
    } else if (statsB.shooting > statsA.shooting + 0.05) {
        factors.add("Attention à la précision chirurgicale de l'adversaire.")
    }
    
    if (factors.isEmpty()) {
        factors.add("Match très équilibré en perspective.")
        factors.add("Le banc pourrait faire la différence.")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "FACTEURS X",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.2.sp
        )
        
        factors.take(2).forEach { factor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4722B).copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFF4722B).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔥", modifier = Modifier.padding(end = 12.dp))
                Text(
                    text = factor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

private data class CalculatedStats(
    val shooting: Double,
    val defense: Double,
    val playmaking: Double,
    val dominance: Double
)

private fun calculateTeamStats(players: List<PlayerScore>): CalculatedStats {
    if (players.isEmpty()) return CalculatedStats(0.0, 0.0, 0.0, 0.0)
    
    return CalculatedStats(
        shooting = players.map { (it.fgPercentile + it.fg3Percentile + it.ftPercentile) / 300.0 }.average(),
        defense = players.map { (it.stlPercentile + it.blkPercentile) / 200.0 }.average(),
        playmaking = players.map { it.astPercentile / 100.0 }.average(),
        dominance = players.map { it.impactScore / 100.0 }.average()
    )
}
