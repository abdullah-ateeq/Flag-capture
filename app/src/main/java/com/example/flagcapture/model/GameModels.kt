package com.example.flagcapture.model

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO;

    fun opponent(): Player = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    fun displayName(): String = if (this == PLAYER_ONE) "Player 1" else "Player 2"
}

enum class PieceType {
    SOLDIER,
    FLAG
}

enum class WinningReason {
    FLAG_CAPTURED,
    OPPONENT_DEFEATED
}

data class GridPosition(
    val row: Int,
    val col: Int
) {
    init {
        require(row in 0..7 && col in 0..7) { "GridPosition out of 8x8 bounds: ($row, $col)" }
    }

    companion object {
        fun isValid(row: Int, col: Int): Boolean = row in 0..7 && col in 0..7
    }
}

data class GamePiece(
    val id: Int,
    val owner: Player,
    val type: PieceType,
    val position: GridPosition,
    val revealed: Boolean = false
)

data class CombatEvent(
    val attacker: GamePiece,
    val defender: GamePiece,
    val isFlagCapture: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameStateSnapshot(
    val pieces: List<GamePiece>,
    val currentPlayer: Player,
    val captures: Int,
    val playerOneCaptures: Int,
    val playerTwoCaptures: Int,
    val turnCount: Int,
    val winner: Player?,
    val winningReason: WinningReason?
)

data class FlagCaptureState(
    val pieces: List<GamePiece> = emptyList(),
    val blockedCells: Set<GridPosition> = emptySet(),
    val currentPlayer: Player = Player.PLAYER_ONE,
    val selectedPieceId: Int? = null,
    val legalMoves: Set<GridPosition> = emptySet(),
    val captures: Int = 0,
    val playerOneCaptures: Int = 0,
    val playerTwoCaptures: Int = 0,
    val turnCount: Int = 0,
    val winner: Player? = null,
    val winningReason: WinningReason? = null,
    val isAnimating: Boolean = false,
    val lastMoveFrom: GridPosition? = null,
    val lastMoveTo: GridPosition? = null,
    val lastCombatEvent: CombatEvent? = null,
    val undoAvailable: Boolean = false,
    val matchStartTimeMs: Long = System.currentTimeMillis(),
    val matchDurationSeconds: Long = 0L
)
