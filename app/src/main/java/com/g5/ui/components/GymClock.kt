package com.g5.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SevenSegmentDigit(
    digit: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawIntoCanvas { canvas ->
            // Inclinaison légère pour l'aspect scoreboard
            canvas.nativeCanvas.skew(-0.1f, 0f)
            
            val thickness = size.width * 0.15f
            val w = size.width
            val h = size.height
            val hLen = w - (thickness * 2)
            val vLen = (h - (thickness * 3)) / 2
            val corner = CornerRadius(thickness / 3)

            val states = when (digit) {
                0 -> listOf(true, true, true, true, true, true, false)
                1 -> listOf(false, true, true, false, false, false, false)
                2 -> listOf(true, true, false, true, true, false, true)
                3 -> listOf(true, true, true, true, false, false, true)
                4 -> listOf(false, true, true, false, false, true, true)
                5 -> listOf(true, false, true, true, false, true, true)
                6 -> listOf(true, false, true, true, true, true, true)
                7 -> listOf(true, true, true, false, false, false, false)
                8 -> listOf(true, true, true, true, true, true, true)
                9 -> listOf(true, true, true, true, false, true, true)
                else -> listOf(false, false, false, false, false, false, false)
            }

            fun drawSegment(state: Boolean, topLeft: Offset, size: Size) {
                drawRoundRect(
                    color = if (state) activeColor else inactiveColor,
                    topLeft = topLeft,
                    size = size,
                    cornerRadius = corner
                )
            }

            drawSegment(states[0], Offset(thickness, 0f), Size(hLen, thickness))
            drawSegment(states[1], Offset(w - thickness, thickness), Size(thickness, vLen))
            drawSegment(states[2], Offset(w - thickness, (thickness * 2) + vLen), Size(thickness, vLen))
            drawSegment(states[3], Offset(thickness, h - thickness), Size(hLen, thickness))
            drawSegment(states[4], Offset(0f, (thickness * 2) + vLen), Size(thickness, vLen))
            drawSegment(states[5], Offset(0f, thickness), Size(thickness, vLen))
            drawSegment(states[6], Offset(thickness, thickness + vLen), Size(hLen, thickness))
        }
    }
}

@Composable
fun LedClock(
    secondsRemaining: Int,
    digitWidth: Dp,
    digitHeight: Dp,
    activeColor: Color = Color(0xFFFF0000),
    inactiveColor: Color = Color(0xFF220000)
) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    
    val m1 = minutes / 10
    val m2 = minutes % 10
    val s1 = seconds / 10
    val s2 = seconds % 10

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SevenSegmentDigit(m1, activeColor, inactiveColor, Modifier.size(digitWidth, digitHeight))
        SevenSegmentDigit(m2, activeColor, inactiveColor, Modifier.size(digitWidth, digitHeight))
        
        // Colon
        Column(
            modifier = Modifier.height(digitHeight),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(digitWidth / 4).background(activeColor, RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(digitWidth / 4))
            Box(Modifier.size(digitWidth / 4).background(activeColor, RoundedCornerShape(2.dp)))
        }
        
        SevenSegmentDigit(s1, activeColor, inactiveColor, Modifier.size(digitWidth, digitHeight))
        SevenSegmentDigit(s2, activeColor, inactiveColor, Modifier.size(digitWidth, digitHeight))
    }
}

@Composable
fun GymClock(secondsRemaining: Int) {
    Box(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.graphicsLayer {
            this.shadowElevation = 20f
            this.spotShadowColor = Color.Red
            this.ambientShadowColor = Color.Red
        }) {
            LedClock(
                secondsRemaining = secondsRemaining,
                digitWidth = 28.dp,
                digitHeight = 48.dp,
                activeColor = Color(0xFFFF0000),
                inactiveColor = Color(0xFF220000)
            )
        }
    }
}

@Composable
fun GymActionTime(secondsRemaining: Int) {
    Box(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        LedClock(
            secondsRemaining = secondsRemaining,
            digitWidth = 8.dp,
            digitHeight = 14.dp,
            activeColor = Color(0xFFFF0000),
            inactiveColor = Color(0xFF110000)
        )
    }
}
