package com.example.flagcapture.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flagcapture.model.Player
import com.example.flagcapture.ui.theme.FlagGold
import com.example.flagcapture.ui.theme.Player1Container
import com.example.flagcapture.ui.theme.Player1Light
import com.example.flagcapture.ui.theme.Player1Primary
import com.example.flagcapture.ui.theme.Player2Container
import com.example.flagcapture.ui.theme.Player2Light
import com.example.flagcapture.ui.theme.Player2Primary

@Composable
fun PlayerHudPanel(
    player: Player,
    piecesCount: Int,
    capturesCount: Int,
    isCurrentTurn: Boolean,
    modifier: Modifier = Modifier
) {
    val isP1 = player == Player.PLAYER_ONE
    val primaryColor = if (isP1) Player1Primary else Player2Primary
    val lightColor = if (isP1) Player1Light else Player2Light
    val containerColor = if (isP1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val borderColor = if (isCurrentTurn) primaryColor else MaterialTheme.colorScheme.outline

    val animatedBorderColor by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(300),
        label = "border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrentTurn) containerColor else MaterialTheme.colorScheme.surface)
            .border(if (isCurrentTurn) 1.8.dp else 1.dp, animatedBorderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player title and turn status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentTurn) {
                    val pulseTransition = rememberInfiniteTransition(label = "pulseHud")
                    val pulseScale by pulseTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "hudPulse"
                    )
                    Box(
                        modifier = Modifier
                            .size((10 * pulseScale).dp)
                            .clip(CircleShape)
                            .background(lightColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = if (isP1) "PLAYER 1" else "PLAYER 2",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentTurn) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    if (isCurrentTurn) {
                        Text(
                            text = "ACTIVE TURN",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Stats counters
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalStatBadge(
                    icon = Icons.Default.Shield,
                    label = "PIECES",
                    value = piecesCount.toString(),
                    tint = primaryColor
                )
                TacticalStatBadge(
                    icon = Icons.Default.Star,
                    label = "CAPTURES",
                    value = capturesCount.toString(),
                    tint = FlagGold
                )
            }
        }
    }
}

@Composable
fun TacticalStatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.8.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun TurnIndicatorHeader(
    currentPlayer: Player,
    turnCount: Int,
    modifier: Modifier = Modifier
) {
    val isP1 = currentPlayer == Player.PLAYER_ONE
    val turnText = if (isP1) "PLAYER 1 TURN" else "PLAYER 2 TURN"
    val turnColor = if (isP1) Player1Primary else Player2Primary
    val turnBg = if (isP1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(turnBg)
            .border(1.dp, turnColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(turnColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = turnText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = turnColor,
                letterSpacing = 1.2.sp
            )
        }

        Text(
            text = "TURN $turnCount",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GameActionControls(
    undoAvailable: Boolean,
    isAnimating: Boolean,
    onUndoClick: () -> Unit,
    onRestartClick: () -> Unit,
    onHowToPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRestartDialog by remember { mutableStateOf(false) }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = {
                Text(
                    text = "Restart this battle?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "The current game will be discarded and reset to the original setup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartDialog = false
                        onRestartClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Player1Primary)
                ) {
                    Text("Restart", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo button
        OutlinedButton(
            onClick = onUndoClick,
            enabled = undoAvailable && !isAnimating,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo move",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Undo", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Restart button
        OutlinedButton(
            onClick = { showRestartDialog = true },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart game",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restart", style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Guide / Briefing button
        IconButton(
            onClick = onHowToPlayClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "How to play",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
