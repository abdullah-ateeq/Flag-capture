package com.example.flagcapture.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flagcapture.model.BoardThemeMode
import com.example.flagcapture.model.FlagCaptureState
import com.example.flagcapture.model.GamePiece
import com.example.flagcapture.model.GridPosition
import com.example.flagcapture.model.PieceType
import com.example.flagcapture.model.Player
import com.example.flagcapture.ui.theme.ClassicBoardDark
import com.example.flagcapture.ui.theme.ClassicCellA
import com.example.flagcapture.ui.theme.ClassicCellB
import com.example.flagcapture.ui.theme.ClassicGridLine
import com.example.flagcapture.ui.theme.FlagGold
import com.example.flagcapture.ui.theme.FlagGoldBright
import com.example.flagcapture.ui.theme.FlagGoldContainer
import com.example.flagcapture.ui.theme.FlagGoldDark
import com.example.flagcapture.ui.theme.LastMoveHighlight
import com.example.flagcapture.ui.theme.LegalCaptureIndicator
import com.example.flagcapture.ui.theme.LegalMoveIndicator
import com.example.flagcapture.ui.theme.MidnightBoardDark
import com.example.flagcapture.ui.theme.MidnightCellA
import com.example.flagcapture.ui.theme.MidnightCellB
import com.example.flagcapture.ui.theme.MidnightGridLine
import com.example.flagcapture.ui.theme.ObstacleStoneBorder
import com.example.flagcapture.ui.theme.ObstacleStoneDark
import com.example.flagcapture.ui.theme.ObstacleStoneLight
import com.example.flagcapture.ui.theme.Player1Border
import com.example.flagcapture.ui.theme.Player1Container
import com.example.flagcapture.ui.theme.Player1Light
import com.example.flagcapture.ui.theme.Player1Primary
import com.example.flagcapture.ui.theme.Player2Border
import com.example.flagcapture.ui.theme.Player2Container
import com.example.flagcapture.ui.theme.Player2Light
import com.example.flagcapture.ui.theme.Player2Primary
import com.example.flagcapture.ui.theme.SelectedCellHighlight

@Composable
fun TacticalGameBoard(
    gameState: FlagCaptureState,
    boardTheme: BoardThemeMode,
    showLegalMoves: Boolean,
    onCellClick: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardBackground = if (boardTheme == BoardThemeMode.MIDNIGHT) MidnightBoardDark else ClassicBoardDark
    val borderStroke = if (boardTheme == BoardThemeMode.MIDNIGHT) MidnightGridLine else ClassicGridLine

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(10.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(boardBackground)
            .border(2.dp, borderStroke, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (row in 0..7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..7) {
                        val pos = GridPosition(row, col)
                        val piece = gameState.pieces.find { it.position == pos }
                        val isBlocked = pos in gameState.blockedCells
                        val isSelected = gameState.selectedPieceId != null &&
                                gameState.pieces.find { it.id == gameState.selectedPieceId }?.position == pos
                        val isLegalMove = showLegalMoves && pos in gameState.legalMoves
                        val isLastMoveSrc = gameState.lastMoveFrom == pos
                        val isLastMoveDst = gameState.lastMoveTo == pos

                        TacticalBoardCell(
                            position = pos,
                            piece = piece,
                            isBlocked = isBlocked,
                            isSelected = isSelected,
                            isLegalMove = isLegalMove,
                            isLastMove = isLastMoveSrc || isLastMoveDst,
                            boardTheme = boardTheme,
                            currentPlayer = gameState.currentPlayer,
                            onClick = { onCellClick(pos) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TacticalBoardCell(
    position: GridPosition,
    piece: GamePiece?,
    isBlocked: Boolean,
    isSelected: Boolean,
    isLegalMove: Boolean,
    isLastMove: Boolean,
    boardTheme: BoardThemeMode,
    currentPlayer: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEven = (position.row + position.col) % 2 == 0
    val cellBaseColor = if (boardTheme == BoardThemeMode.MIDNIGHT) {
        if (isEven) MidnightCellA else MidnightCellB
    } else {
        if (isEven) ClassicCellA else ClassicCellB
    }

    val accessibilityDesc = buildString {
        append("Row ${position.row + 1}, Column ${position.col + 1}. ")
        when {
            isBlocked -> append("Blocked stone obstacle.")
            piece != null -> {
                if (piece.owner == currentPlayer) {
                    append("${currentPlayer.displayName()} ${piece.type.name.lowercase()}. ")
                } else if (piece.revealed) {
                    append("Revealed enemy ${piece.type.name.lowercase()}. ")
                } else {
                    append("Hidden enemy piece. ")
                }
            }
            else -> append("Empty terrain. ")
        }
        if (isSelected) append("Selected. ")
        if (isLegalMove) append("Legal move destination. ")
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(cellBaseColor)
            .semantics { contentDescription = accessibilityDesc }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Last move subtle highlight
        if (isLastMove) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LastMoveHighlight.copy(alpha = 0.15f))
            )
        }

        // Selection highlight border
        if (isSelected) {
            val pulseTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by pulseTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, SelectedCellHighlight.copy(alpha = pulseAlpha), RoundedCornerShape(4.dp))
                    .background(SelectedCellHighlight.copy(alpha = 0.18f))
            )
        }

        // Blocked stone obstacle rendering
        if (isBlocked) {
            ObstacleView(modifier = Modifier.fillMaxSize())
        }

        // Piece rendering
        if (piece != null) {
            PieceView(
                piece = piece,
                viewer = currentPlayer,
                modifier = Modifier.fillMaxSize(0.85f)
            )
        }

        // Legal move indicator
        if (isLegalMove) {
            val isCaptureTarget = piece != null && piece.owner != currentPlayer
            LegalMoveTargetIndicator(
                isCapture = isCaptureTarget,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ObstacleView(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(ObstacleStoneDark)
            .border(1.dp, ObstacleStoneBorder, RoundedCornerShape(3.dp))
    ) {
        val stroke = 1.2.dp.toPx()
        val color = ObstacleStoneLight

        // Tactical diagonal crosshatch markings
        val step = size.width / 3f
        drawLine(
            color = color,
            start = Offset(0f, step),
            end = Offset(step, 0f),
            strokeWidth = stroke
        )
        drawLine(
            color = color,
            start = Offset(0f, step * 2),
            end = Offset(step * 2, 0f),
            strokeWidth = stroke
        )
        drawLine(
            color = color,
            start = Offset(step, size.height),
            end = Offset(size.width, step),
            strokeWidth = stroke
        )
        drawLine(
            color = color,
            start = Offset(step * 2, size.height),
            end = Offset(size.width, step * 2),
            strokeWidth = stroke
        )
    }
}

@Composable
fun PieceView(
    piece: GamePiece,
    viewer: Player,
    modifier: Modifier = Modifier
) {
    val isFriendly = piece.owner == viewer
    val isRevealed = piece.revealed

    if (isFriendly) {
        // Own piece: visible identity
        if (piece.type == PieceType.FLAG) {
            OwnFlagView(owner = piece.owner, modifier = modifier)
        } else {
            OwnSoldierView(owner = piece.owner, modifier = modifier)
        }
    } else {
        // Opponent piece
        if (isRevealed) {
            // Revealed during combat
            if (piece.type == PieceType.FLAG) {
                RevealedEnemyFlagView(owner = piece.owner, modifier = modifier)
            } else {
                RevealedEnemySoldierView(owner = piece.owner, modifier = modifier)
            }
        } else {
            // Hidden enemy marker
            HiddenEnemyMarkerView(owner = piece.owner, modifier = modifier)
        }
    }
}

@Composable
fun OwnSoldierView(owner: Player, modifier: Modifier = Modifier) {
    val isP1 = owner == Player.PLAYER_ONE
    val primaryColor = if (isP1) Player1Primary else Player2Primary
    val lightColor = if (isP1) Player1Light else Player2Light
    val containerColor = if (isP1) Player1Container else Player2Container
    val borderColor = if (isP1) Player1Border else Player2Border

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(listOf(containerColor, containerColor.copy(alpha = 0.85f))))
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.fillMaxSize(0.65f)
        )
        // Tactical unit badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(lightColor)
        )
    }
}

@Composable
fun OwnFlagView(owner: Player, modifier: Modifier = Modifier) {
    val isP1 = owner == Player.PLAYER_ONE
    val ownerColor = if (isP1) Player1Primary else Player2Primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.radialGradient(listOf(FlagGoldContainer, Color(0xFF1E1706))))
            .border(2.dp, FlagGold, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = FlagGoldBright,
            modifier = Modifier.fillMaxSize(0.72f)
        )
        // Subtle owner dot at the top
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(ownerColor)
        )
    }
}

@Composable
fun HiddenEnemyMarkerView(owner: Player, modifier: Modifier = Modifier) {
    val isP1 = owner == Player.PLAYER_ONE
    val glowColor = if (isP1) Player1Light.copy(alpha = 0.6f) else Player2Light.copy(alpha = 0.6f)
    val borderColor = if (isP1) Player1Border else Player2Border

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF191E28))
            .border(1.5.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Unknown tactical marker symbol (?)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            tint = glowColor,
            modifier = Modifier.fillMaxSize(0.6f)
        )
    }
}

@Composable
fun RevealedEnemyFlagView(owner: Player, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.radialGradient(listOf(Color(0xFF4A1009), Color(0xFF1A0604))))
            .border(2.dp, LegalCaptureIndicator, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = FlagGoldBright,
            modifier = Modifier.fillMaxSize(0.72f)
        )
    }
}

@Composable
fun RevealedEnemySoldierView(owner: Player, modifier: Modifier = Modifier) {
    val isP1 = owner == Player.PLAYER_ONE
    val color = if (isP1) Player1Primary else Player2Primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2B1A1A))
            .border(1.5.dp, LegalCaptureIndicator, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = color,
            modifier = Modifier.fillMaxSize(0.65f)
        )
    }
}

@Composable
fun LegalMoveTargetIndicator(isCapture: Boolean, modifier: Modifier = Modifier) {
    if (isCapture) {
        // Reticle target for capturing enemy piece
        Canvas(modifier = modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val reticleColor = LegalCaptureIndicator
            val w = size.width
            val h = size.height
            val cornerLen = w * 0.22f

            // Top-left corner
            drawLine(reticleColor, Offset(3f, 3f), Offset(3f + cornerLen, 3f), strokeWidth = stroke)
            drawLine(reticleColor, Offset(3f, 3f), Offset(3f, 3f + cornerLen), strokeWidth = stroke)

            // Top-right corner
            drawLine(reticleColor, Offset(w - 3f, 3f), Offset(w - 3f - cornerLen, 3f), strokeWidth = stroke)
            drawLine(reticleColor, Offset(w - 3f, 3f), Offset(w - 3f, 3f + cornerLen), strokeWidth = stroke)

            // Bottom-left corner
            drawLine(reticleColor, Offset(3f, h - 3f), Offset(3f + cornerLen, h - 3f), strokeWidth = stroke)
            drawLine(reticleColor, Offset(3f, h - 3f), Offset(3f, h - 3f - cornerLen), strokeWidth = stroke)

            // Bottom-right corner
            drawLine(reticleColor, Offset(w - 3f, h - 3f), Offset(w - 3f - cornerLen, h - 3f), strokeWidth = stroke)
            drawLine(reticleColor, Offset(w - 3f, h - 3f), Offset(w - 3f, h - 3f - cornerLen), strokeWidth = stroke)
        }
    } else {
        // Subtle emerald tactical target dot
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(LegalMoveIndicator)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}
