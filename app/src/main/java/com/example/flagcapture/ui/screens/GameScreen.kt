package com.example.flagcapture.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flagcapture.model.BoardThemeMode
import com.example.flagcapture.model.Player
import com.example.flagcapture.model.WinningReason
import com.example.flagcapture.ui.components.GameActionControls
import com.example.flagcapture.ui.components.PlayerHudPanel
import com.example.flagcapture.ui.components.TacticalGameBoard
import com.example.flagcapture.ui.components.TurnIndicatorHeader
import com.example.flagcapture.ui.theme.BorderDark
import com.example.flagcapture.ui.theme.CharcoalDark
import com.example.flagcapture.ui.theme.FlagGold
import com.example.flagcapture.ui.theme.FlagGoldBright
import com.example.flagcapture.ui.theme.Player1Light
import com.example.flagcapture.ui.theme.Player1Primary
import com.example.flagcapture.ui.theme.Player2Light
import com.example.flagcapture.ui.theme.Player2Primary
import com.example.flagcapture.ui.theme.SurfaceCardDark
import com.example.flagcapture.ui.theme.SurfaceDark
import com.example.flagcapture.ui.theme.TextPrimaryDark
import com.example.flagcapture.ui.theme.TextSecondaryDark
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (winner: String, reason: String, captures: Int, turns: Int, p1Rem: Int, p2Rem: Int, duration: Long) -> Unit,
    onHowToPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val settings by gameViewModel.settings.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Collect result navigation event
    LaunchedEffect(Unit) {
        gameViewModel.gameResultEvent.collectLatest { result ->
            onNavigateToResult(
                result.winner.name,
                result.reason.name,
                result.captures,
                result.turns,
                result.p1Remaining,
                result.p2Remaining,
                result.durationSeconds
            )
        }
    }

    val p1PiecesCount = gameState.pieces.count { it.owner == Player.PLAYER_ONE }
    val p2PiecesCount = gameState.pieces.count { it.owner == Player.PLAYER_TWO }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (isLandscape) {
            // Landscape / Tablet layout: Board on left, info & controls on right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Board on Left (constrained to square)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    TacticalGameBoard(
                        gameState = gameState,
                        boardTheme = settings.boardTheme,
                        showLegalMoves = settings.showLegalMoves,
                        onCellClick = { gameViewModel.onCellClicked(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Controls & HUD on Right
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "FLAG CAPTURE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(modifier = Modifier.size(36.dp))
                    }

                    // Turn indicator
                    TurnIndicatorHeader(
                        currentPlayer = gameState.currentPlayer,
                        turnCount = gameState.turnCount
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Player 2 Status
                    PlayerHudPanel(
                        player = Player.PLAYER_TWO,
                        piecesCount = p2PiecesCount,
                        capturesCount = gameState.playerTwoCaptures,
                        isCurrentTurn = gameState.currentPlayer == Player.PLAYER_TWO
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Player 1 Status
                    PlayerHudPanel(
                        player = Player.PLAYER_ONE,
                        piecesCount = p1PiecesCount,
                        capturesCount = gameState.playerOneCaptures,
                        isCurrentTurn = gameState.currentPlayer == Player.PLAYER_ONE
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Controls
                    GameActionControls(
                        undoAvailable = gameState.undoAvailable,
                        isAnimating = gameState.isAnimating,
                        onUndoClick = { gameViewModel.undoMove() },
                        onRestartClick = { gameViewModel.restartGame() },
                        onHowToPlayClick = onHowToPlayClick
                    )
                }
            }
        } else {
            // Portrait Layout: Player 2 Top, Board Center, Player 1 Bottom, Controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to home",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TurnIndicatorHeader(
                        currentPlayer = gameState.currentPlayer,
                        turnCount = gameState.turnCount,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    Box(modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Player 2 Status (Top)
                PlayerHudPanel(
                    player = Player.PLAYER_TWO,
                    piecesCount = p2PiecesCount,
                    capturesCount = gameState.playerTwoCaptures,
                    isCurrentTurn = gameState.currentPlayer == Player.PLAYER_TWO
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 8x8 Board (Center)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = minOf(maxWidth, maxHeight)
                    TacticalGameBoard(
                        gameState = gameState,
                        boardTheme = settings.boardTheme,
                        showLegalMoves = settings.showLegalMoves,
                        onCellClick = { gameViewModel.onCellClicked(it) },
                        modifier = Modifier.size(boardSize)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Player 1 Status (Bottom)
                PlayerHudPanel(
                    player = Player.PLAYER_ONE,
                    piecesCount = p1PiecesCount,
                    capturesCount = gameState.playerOneCaptures,
                    isCurrentTurn = gameState.currentPlayer == Player.PLAYER_ONE
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Controls Bar
                GameActionControls(
                    undoAvailable = gameState.undoAvailable,
                    isAnimating = gameState.isAnimating,
                    onUndoClick = { gameViewModel.undoMove() },
                    onRestartClick = { gameViewModel.restartGame() },
                    onHowToPlayClick = onHowToPlayClick
                )

                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Victory celebration banner overlay (shown briefly before auto-navigation)
        AnimatedVisibility(
            visible = gameState.winner != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val winner = gameState.winner
            val winnerColor = if (winner == Player.PLAYER_ONE) Player1Primary else Player2Primary
            val reasonText = if (gameState.winningReason == WinningReason.FLAG_CAPTURED) {
                "ENEMY FLAG CAPTURED!"
            } else {
                "OPPONENT ARMY ELIMINATED!"
            }

            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, FlagGold, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = FlagGoldBright,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "${winner?.displayName()?.uppercase()} WINS",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = winnerColor,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = reasonText,
                        style = MaterialTheme.typography.labelMedium,
                        color = FlagGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Preparing tactical debrief...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
