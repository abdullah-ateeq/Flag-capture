package com.example.flagcapture.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flagcapture.ui.theme.BorderDark
import com.example.flagcapture.ui.theme.CharcoalDark
import com.example.flagcapture.ui.theme.FlagGold
import com.example.flagcapture.ui.theme.FlagGoldBright
import com.example.flagcapture.ui.theme.Player1Light
import com.example.flagcapture.ui.theme.Player1Primary
import com.example.flagcapture.ui.theme.Player2Light
import com.example.flagcapture.ui.theme.Player2Primary
import com.example.flagcapture.ui.theme.TextMutedDark
import com.example.flagcapture.ui.theme.TextPrimaryDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animation stages
    val gridAlpha = remember { Animatable(0f) }
    val flagsAlpha = remember { Animatable(0f) }
    val pieceProgress = remember { Animatable(0f) }
    val captureClash = remember { Animatable(0f) }
    val flagRevealed = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        // Step 1: Grid fades in (0-300ms)
        gridAlpha.animateTo(1f, animationSpec = tween(300, easing = LinearEasing))

        // Step 2: Two flags appear (300-600ms)
        flagsAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))

        // Step 3: Pieces move forward (600-950ms)
        pieceProgress.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))

        // Step 4: Capture happens (950-1200ms)
        captureClash.animateTo(1f, animationSpec = tween(250, easing = FastOutSlowInEasing))

        // Step 5: Enemy flag is revealed (1200-1400ms)
        flagRevealed.animateTo(1f, animationSpec = tween(200, easing = FastOutSlowInEasing))

        // Step 6: Title appears (1400-1700ms)
        titleScale.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
        titleAlpha.animateTo(1f, animationSpec = tween(300, easing = LinearEasing))

        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B202A), CharcoalDark),
                    center = Offset.Zero
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Tactical Animation Stage Canvas
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141720))
                    .alpha(gridAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Tactical Mini 4x4 Grid
                Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val step = size.width / 4f
                    for (i in 0..4) {
                        drawLine(
                            color = BorderDark.copy(alpha = 0.6f),
                            start = Offset(i * step, 0f),
                            end = Offset(i * step, size.height),
                            strokeWidth = 1.2.dp.toPx()
                        )
                        drawLine(
                            color = BorderDark.copy(alpha = 0.6f),
                            start = Offset(0f, i * step),
                            end = Offset(size.width, i * step),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                }

                // Flags appearing at top and bottom
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .alpha(flagsAlpha.value)
                ) {
                    // Top Player 2 Flag
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = if (flagRevealed.value > 0.5f) FlagGoldBright else Player2Light,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.TopCenter)
                            .scale(if (flagRevealed.value > 0.5f) 1.25f else 1f)
                    )

                    // Bottom Player 1 Flag
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = FlagGoldBright,
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.BottomCenter)
                    )

                    // Pieces moving forward and clashing
                    val clashY = 0.5f
                    val p1PieceY = 0.85f - (pieceProgress.value * 0.35f)
                    val p2PieceY = 0.15f + (pieceProgress.value * 0.35f)

                    if (captureClash.value < 0.8f) {
                        // Attacking pieces
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Player1Light,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                                .scale(1f - (captureClash.value * 0.5f))
                        )
                    } else {
                        // Victorious flag discovery pulse
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .background(FlagGold.copy(alpha = 0.35f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Title & Subtitle Fade-in
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .scale(titleScale.value)
            ) {
                Text(
                    text = "FLAG CAPTURE",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TACTICAL STRATEGY COMBAT",
                    style = MaterialTheme.typography.labelMedium,
                    color = FlagGold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tap to begin",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
