package com.example.flagcapture.game

import com.example.flagcapture.model.CombatEvent
import com.example.flagcapture.model.FlagCaptureState
import com.example.flagcapture.model.GamePiece
import com.example.flagcapture.model.GameStateSnapshot
import com.example.flagcapture.model.GridPosition
import com.example.flagcapture.model.PieceType
import com.example.flagcapture.model.Player
import com.example.flagcapture.model.WinningReason

object GameRules {

    /**
     * 6 symmetrically placed blocked obstacle cells in the neutral zone.
     * Guaranteed open corridors across the entire board.
     */
    val DEFAULT_BLOCKED_CELLS: Set<GridPosition> = setOf(
        GridPosition(3, 1),
        GridPosition(3, 3),
        GridPosition(3, 6),
        GridPosition(4, 1),
        GridPosition(4, 4),
        GridPosition(4, 6)
    )

    /**
     * Creates the initial symmetric 8x8 game board setup.
     * Player 2 occupies rows 0 and 1.
     * Player 1 occupies rows 6 and 7.
     * Each player has 1 Flag and 5 Soldiers (6 pieces total).
     */
    fun createInitialState(blockedCells: Set<GridPosition> = DEFAULT_BLOCKED_CELLS): FlagCaptureState {
        val pieces = mutableListOf<GamePiece>()
        var idCounter = 1

        // Player 2 setup (Top rows 0 and 1)
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.FLAG, position = GridPosition(0, 3)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(0, 2)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(0, 4)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(1, 2)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(1, 3)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(1, 4)))

        // Player 1 setup (Bottom rows 6 and 7) - 180 degree rotation symmetric
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.FLAG, position = GridPosition(7, 4)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(7, 5)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(7, 3)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(6, 5)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(6, 4)))
        pieces.add(GamePiece(id = idCounter++, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(6, 3)))

        return FlagCaptureState(
            pieces = pieces,
            blockedCells = blockedCells,
            currentPlayer = Player.PLAYER_ONE,
            selectedPieceId = null,
            legalMoves = emptySet(),
            captures = 0,
            playerOneCaptures = 0,
            playerTwoCaptures = 0,
            turnCount = 1,
            winner = null,
            winningReason = null,
            isAnimating = false,
            undoAvailable = false,
            matchStartTimeMs = System.currentTimeMillis()
        )
    }

    /**
     * Orthogonal neighbors (Up, Down, Left, Right) inside the 8x8 board.
     */
    fun getNeighbors(position: GridPosition): List<GridPosition> {
        val neighbors = mutableListOf<GridPosition>()
        val (row, col) = position

        if (row > 0) neighbors.add(GridPosition(row - 1, col)) // Up
        if (row < 7) neighbors.add(GridPosition(row + 1, col)) // Down
        if (col > 0) neighbors.add(GridPosition(row, col - 1)) // Left
        if (col < 7) neighbors.add(GridPosition(row, col + 1)) // Right

        return neighbors
    }

    /**
     * Determines legal movement destinations for a piece.
     * - Flags cannot move.
     * - Only current player's pieces can move.
     * - Movement is 1 cell orthogonally.
     * - Cannot enter blocked cells.
     * - Cannot enter cells occupied by friendly pieces (soldier or flag).
     */
    fun getLegalMoves(piece: GamePiece, state: FlagCaptureState): Set<GridPosition> {
        if (piece.type == PieceType.FLAG) return emptySet()
        if (piece.owner != state.currentPlayer) return emptySet()
        if (state.winner != null) return emptySet()

        val friendlyPositions = state.pieces
            .filter { it.owner == piece.owner }
            .map { it.position }
            .toSet()

        return getNeighbors(piece.position)
            .filter { target ->
                target !in state.blockedCells && target !in friendlyPositions
            }
            .toSet()
    }

    /**
     * Creates a snapshot of the current state for Undo.
     */
    fun createSnapshot(state: FlagCaptureState): GameStateSnapshot {
        return GameStateSnapshot(
            pieces = state.pieces.map { it.copy() },
            currentPlayer = state.currentPlayer,
            captures = state.captures,
            playerOneCaptures = state.playerOneCaptures,
            playerTwoCaptures = state.playerTwoCaptures,
            turnCount = state.turnCount,
            winner = state.winner,
            winningReason = state.winningReason
        )
    }

    sealed class MoveOutcome {
        data class Success(val newState: FlagCaptureState, val combatEvent: CombatEvent? = null) : MoveOutcome()
        data class Invalid(val reason: String) : MoveOutcome()
    }

    /**
     * Executes a piece move and resolves combat and victory conditions.
     */
    fun movePiece(
        pieceId: Int,
        destination: GridPosition,
        state: FlagCaptureState
    ): MoveOutcome {
        if (state.winner != null) {
            return MoveOutcome.Invalid("Game is already finished")
        }
        val piece = state.pieces.find { it.id == pieceId }
            ?: return MoveOutcome.Invalid("Piece not found")

        if (piece.owner != state.currentPlayer) {
            return MoveOutcome.Invalid("Not current player's piece")
        }
        if (piece.type == PieceType.FLAG) {
            return MoveOutcome.Invalid("Flags cannot move")
        }

        val legalMoves = getLegalMoves(piece, state)
        if (destination !in legalMoves) {
            return MoveOutcome.Invalid("Destination is not a legal move")
        }

        val targetPiece = state.pieces.find { it.position == destination }
        val remainingPieces = state.pieces.filter { it.id != piece.id }.toMutableList()

        var combatEvent: CombatEvent? = null
        var p1Captures = state.playerOneCaptures
        var p2Captures = state.playerTwoCaptures
        var totalCaptures = state.captures
        var winner: Player? = null
        var winningReason: WinningReason? = null

        if (targetPiece == null) {
            // Moved to empty cell
            remainingPieces.add(piece.copy(position = destination))
        } else {
            // Combat with enemy piece
            if (targetPiece.type == PieceType.FLAG) {
                // Attacker captured enemy flag!
                combatEvent = CombatEvent(
                    attacker = piece.copy(position = destination),
                    defender = targetPiece.copy(revealed = true),
                    isFlagCapture = true
                )
                // Reveal flag and place winning soldier on destination
                remainingPieces.removeAll { it.id == targetPiece.id }
                remainingPieces.add(piece.copy(position = destination))
                remainingPieces.add(targetPiece.copy(revealed = true))

                totalCaptures++
                if (state.currentPlayer == Player.PLAYER_ONE) p1Captures++ else p2Captures++
                winner = state.currentPlayer
                winningReason = WinningReason.FLAG_CAPTURED
            } else {
                // Soldier vs Soldier combat: Both pieces are removed!
                combatEvent = CombatEvent(
                    attacker = piece.copy(position = destination),
                    defender = targetPiece.copy(revealed = true),
                    isFlagCapture = false
                )
                remainingPieces.removeAll { it.id == targetPiece.id }
                // Both removed: piece is NOT added back to remainingPieces
                totalCaptures++
                if (state.currentPlayer == Player.PLAYER_ONE) p1Captures++ else p2Captures++

                // Check remaining soldiers for both players
                val opponentSoldiers = remainingPieces.count { it.owner == state.currentPlayer.opponent() && it.type == PieceType.SOLDIER }
                val currentSoldiers = remainingPieces.count { it.owner == state.currentPlayer && it.type == PieceType.SOLDIER }

                if (opponentSoldiers == 0) {
                    winner = state.currentPlayer
                    winningReason = WinningReason.OPPONENT_DEFEATED
                } else if (currentSoldiers == 0) {
                    winner = state.currentPlayer.opponent()
                    winningReason = WinningReason.OPPONENT_DEFEATED
                }
            }
        }

        val nextPlayer = if (winner != null) state.currentPlayer else state.currentPlayer.opponent()
        val nextTurnCount = state.turnCount + 1

        val newState = state.copy(
            pieces = remainingPieces,
            currentPlayer = nextPlayer,
            selectedPieceId = null,
            legalMoves = emptySet(),
            captures = totalCaptures,
            playerOneCaptures = p1Captures,
            playerTwoCaptures = p2Captures,
            turnCount = nextTurnCount,
            winner = winner,
            winningReason = winningReason,
            isAnimating = false,
            lastMoveFrom = piece.position,
            lastMoveTo = destination,
            lastCombatEvent = combatEvent,
            undoAvailable = true
        )

        return MoveOutcome.Success(newState, combatEvent)
    }

    /**
     * Restores previous state from a snapshot.
     */
    fun restoreSnapshot(snapshot: GameStateSnapshot, currentBlocked: Set<GridPosition>): FlagCaptureState {
        return FlagCaptureState(
            pieces = snapshot.pieces.map { it.copy() },
            blockedCells = currentBlocked,
            currentPlayer = snapshot.currentPlayer,
            selectedPieceId = null,
            legalMoves = emptySet(),
            captures = snapshot.captures,
            playerOneCaptures = snapshot.playerOneCaptures,
            playerTwoCaptures = snapshot.playerTwoCaptures,
            turnCount = snapshot.turnCount,
            winner = snapshot.winner,
            winningReason = snapshot.winningReason,
            isAnimating = false,
            lastMoveFrom = null,
            lastMoveTo = null,
            lastCombatEvent = null,
            undoAvailable = false // Updated by caller based on stack size
        )
    }
}
