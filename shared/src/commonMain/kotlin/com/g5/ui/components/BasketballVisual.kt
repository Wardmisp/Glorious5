package com.g5.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BasketballVisual(
    modifier: Modifier = Modifier,
    size: Dp = 112.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 2
        
        // Basketball sphere
        drawCircle(
            color = Color(0xFFF4722B),
            radius = radius,
            center = center
        )
        
        // Basketball outline
        drawCircle(
            color = Color(0xFFC85A1A),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Vertical lines
        drawLine(
            color = Color(0xFFC85A1A),
            start = Offset(center.x, 0f),
            end = Offset(center.x, this.size.height),
            strokeWidth = 3.dp.toPx()
        )
        
        // Horizontal curves (simplified)
        drawLine(
            color = Color(0xFFC85A1A),
            start = Offset(0f, center.y),
            end = Offset(this.size.width, center.y),
            strokeWidth = 3.dp.toPx()
        )
        
        // Curved lines
        drawArc(
            color = Color(0xFFC85A1A),
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(this.size.width, this.size.height),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
