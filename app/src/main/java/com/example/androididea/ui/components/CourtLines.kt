package com.example.androididea.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CourtLines(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val width = size.width
        val height = size.height
        val courtColor = Color(0xFFF4722B).copy(alpha = 0.1f)
        val strokeWidth = 2.dp.toPx()

        // Center circle
        drawCircle(
            color = courtColor,
            radius = height * 0.25f,
            center = Offset(width / 2, height / 2),
            style = Stroke(width = strokeWidth)
        )

        // Center point
        drawCircle(
            color = courtColor,
            radius = 8f,
            center = Offset(width / 2, height / 2)
        )

        // Center line
        drawLine(
            color = courtColor,
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = strokeWidth
        )

        // Left half court
        drawArc(
            color = courtColor,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(-width * 0.25f, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(width * 0.5f, height * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Right half court
        drawArc(
            color = courtColor,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(width * 0.75f, height * 0.25f),
            size = androidx.compose.ui.geometry.Size(width * 0.5f, height * 0.5f),
            style = Stroke(width = strokeWidth)
        )

        // Left basket area
        drawRect(
            color = courtColor,
            topLeft = Offset(0f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.25f, height * 0.4f),
            style = Stroke(width = strokeWidth)
        )

        // Right basket area
        drawRect(
            color = courtColor,
            topLeft = Offset(width * 0.75f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.25f, height * 0.4f),
            style = Stroke(width = strokeWidth)
        )

        // Left basket circle
        drawCircle(
            color = courtColor,
            radius = height * 0.15f,
            center = Offset(width * 0.25f, height / 2),
            style = Stroke(width = strokeWidth)
        )

        // Right basket circle
        drawCircle(
            color = courtColor,
            radius = height * 0.15f,
            center = Offset(width * 0.75f, height / 2),
            style = Stroke(width = strokeWidth)
        )
    }
}
