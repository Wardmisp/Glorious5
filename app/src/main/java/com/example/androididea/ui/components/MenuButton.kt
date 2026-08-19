package com.example.androididea.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuButton(
    icon: ImageVector,
    label: String,
    sublabel: String,
    onClick: () -> Unit,
    variant: MenuButtonVariant = MenuButtonVariant.Default,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        val (backgroundColor, borderColor, iconBackgroundColor, iconColor, textColor, subtextColor) =
            when (variant) {
                MenuButtonVariant.Primary -> {
                    Tuple6(
                        Color(0xFFF4722B).copy(alpha = 0.9f),
                        Color(0xFFF4722B).copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White,
                        Color.White,
                        Color.White.copy(alpha = 0.7f)
                    )
                }
                MenuButtonVariant.Secondary,
                MenuButtonVariant.Default -> {
                    Tuple6(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        Color(0xFFF4722B).copy(alpha = 0.15f),
                        Color(0xFFF4722B),
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = iconBackgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp,
                    color = textColor
                )
                Text(
                    text = sublabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = subtextColor
                )
            }

            Icon(
                imageVector = Icons.Default.Settings, // Placeholder - will use ChevronRight
                contentDescription = null,
                tint = if (variant == MenuButtonVariant.Primary) Color.White.copy(alpha = 0.6f) else Color(0xFFF4722B).copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

enum class MenuButtonVariant {
    Primary,
    Secondary,
    Default
}

// Helper data class for multiple values
data class Tuple6<A, B, C, D, E, F>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F
)
