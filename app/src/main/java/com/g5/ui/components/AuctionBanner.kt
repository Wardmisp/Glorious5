package com.g5.ui.components

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.g5.R

@Composable
fun AuctionBanner(
    currentBid: Int,
    currentBidder: Int?,
    p1Name: String,
    p2Name: String,
    thinking: Boolean,
    done: Boolean,
    awardedTo: Int?,
    timer: Int,
    modifier: Modifier = Modifier
) {
    val leaderName = when (currentBidder) {
        1 -> p1Name
        2 -> p2Name
        else -> null
    }

    val (backgroundColor, borderColor) = if (done) {
        Color(0xFFF59E0B).copy(alpha = 0.05f) to Color(0xFFF59E0B).copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(if (done) R.string.auction_banner_awarded else R.string.auction_banner_current_bid_label),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = if (currentBid == 0) stringResource(R.string.auction_banner_no_bid) else "$$currentBid",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = if (done) Color(0xFFF59E0B) else Color(0xFFF4722B)
            )

            if (!done && currentBid > 0) {
                Text(
                    text = stringResource(R.string.auction_banner_awarded_in, timer),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = if (timer <= 3) Color.Red else Color(0xFFF59E0B)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.4f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (thinking && !done) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.auction_banner_computer_thinking),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    repeat(3) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 700
                                    1f at 233
                                    0.3f at 466
                                }
                            ),
                            label = "dotAlpha_$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(2.dp)
                                .background(
                                    color = Color(0xFFF4722B).copy(alpha = alpha),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            } else if (leaderName != null && !thinking) {
                Text(
                    text = stringResource(if (done) R.string.auction_banner_winner else R.string.auction_banner_leader),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = leaderName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else if (!leaderName.isNullOrEmpty() == false && !thinking) {
                Text(
                    text = stringResource(R.string.auction_banner_start_bidding),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
