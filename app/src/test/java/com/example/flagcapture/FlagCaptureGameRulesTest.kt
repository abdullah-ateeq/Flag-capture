package com.example.flagcapture

import com.example.flagcapture.game.GameRules
import com.example.flagcapture.model.FlagCaptureState
import com.example.flagcapture.model.GamePiece
import com.example.flagcapture.model.GridPosition
import com.example.flagcapture.model.PieceType
import com.example.flagcapture.model.Player
import com.example.flagcapture.model.WinningReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlagCaptureGameRulesTest {

    @Test
    fun testInitialSetup() {
        val state = GameRules.createInitialState()

        // 12 pieces total: 6 for P1, 6 for P2
        assertEquals(12, state.pieces.size)

        val p1Pieces = state.pieces.filter { it.owner == Player.PLAYER_ONE }
        val p2Pieces = state.pieces.filter { it.owner == Player.PLAYER_TWO }

        assertEquals(6, p1Pieces.size)
        assertEquals(6, p2Pieces.size)

        assertEquals(5, p1Pieces.count { it.type == PieceType.SOLDIER })
        assertEquals(1, p1Pieces.count { it.type == PieceType.FLAG })

        assertEquals(5, p2Pieces.count { it.type == PieceType.SOLDIER })
        assertEquals(1, p2Pieces.count { it.type == PieceType.FLAG })

        // Exactly 6 blocked cells
        assertEquals(6, state.blockedCells.size)

        // Starting rows: P2 top two rows (0, 1), P1 bottom two rows (6, 7)
        assertTrue(p2Pieces.all { it.position.row in 0..1 })
        assertTrue(p1Pieces.all { it.position.row in 6..7 })

        // Initial player and turn
        assertEquals(Player.PLAYER_ONE, state.currentPlayer)
        assertEquals(1, state.turnCount)
        assertNull(state.winner)
    }

    @Test
    fun testLegalMovement_orthogonalOnlyAndInsideBoard() {
        val testPieces = listOf(
            GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5)),
            GamePiece(id = 2, owner = Player.PLAYER_ONE, type = PieceType.FLAG, position = GridPosition(7, 4))
        )
        val state = FlagCaptureState(
            pieces = testPieces,
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE
        )

        val soldier = testPieces[0]
        val legalMoves = GameRules.getLegalMoves(soldier, state)

        // Orthogonal 1 cell only: (4, 5), (6, 5), (5, 4), (5, 6)
        assertEquals(4, legalMoves.size)
        assertTrue(legalMoves.contains(GridPosition(4, 5))) // Up
        assertTrue(legalMoves.contains(GridPosition(6, 5))) // Down
        assertTrue(legalMoves.contains(GridPosition(5, 4))) // Left
        assertTrue(legalMoves.contains(GridPosition(5, 6))) // Right

        // No diagonal moves
        assertFalse(legalMoves.contains(GridPosition(4, 4)))
        assertFalse(legalMoves.contains(GridPosition(6, 6)))

        // Flags cannot move
        val flag = testPieces[1]
        val flagMoves = GameRules.getLegalMoves(flag, state)
        assertTrue(flagMoves.isEmpty())
    }

    @Test
    fun testBlockedCells_cannotBeEntered() {
        val blocked = setOf(GridPosition(5, 4))
        val soldier = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5))
        val state = FlagCaptureState(
            pieces = listOf(soldier),
            blockedCells = blocked,
            currentPlayer = Player.PLAYER_ONE
        )

        val legalMoves = GameRules.getLegalMoves(soldier, state)
        assertFalse("Blocked cell should not be a legal move", legalMoves.contains(GridPosition(5, 4)))

        // Attempting to move into blocked cell should fail
        val outcome = GameRules.movePiece(soldier.id, GridPosition(5, 4), state)
        assertTrue(outcome is GameRules.MoveOutcome.Invalid)
    }

    @Test
    fun testFriendlyBlocking_cannotMoveOntoFriendlyPieces() {
        val soldier1 = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5))
        val soldier2 = GamePiece(id = 2, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 4))
        val friendlyFlag = GamePiece(id = 3, owner = Player.PLAYER_ONE, type = PieceType.FLAG, position = GridPosition(6, 5))

        val state = FlagCaptureState(
            pieces = listOf(soldier1, soldier2, friendlyFlag),
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE
        )

        val legalMoves = GameRules.getLegalMoves(soldier1, state)
        assertFalse("Cannot move onto friendly soldier", legalMoves.contains(GridPosition(5, 4)))
        assertFalse("Cannot move onto friendly flag", legalMoves.contains(GridPosition(6, 5)))
    }

    @Test
    fun testCombatRemoval_soldierVsSoldierBothRemoved() {
        val p1Soldier = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5))
        val p2Soldier = GamePiece(id = 2, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(4, 5))
        val p1Flag = GamePiece(id = 3, owner = Player.PLAYER_ONE, type = PieceType.FLAG, position = GridPosition(7, 4))
        val p2Flag = GamePiece(id = 4, owner = Player.PLAYER_TWO, type = PieceType.FLAG, position = GridPosition(0, 3))
        val p2ExtraSoldier = GamePiece(id = 5, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(1, 1))

        val state = FlagCaptureState(
            pieces = listOf(p1Soldier, p2Soldier, p1Flag, p2Flag, p2ExtraSoldier),
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE,
            captures = 0
        )

        val outcome = GameRules.movePiece(p1Soldier.id, GridPosition(4, 5), state)
        assertTrue(outcome is GameRules.MoveOutcome.Success)

        val newState = (outcome as GameRules.MoveOutcome.Success).newState

        // Both soldiers must be removed from the board!
        assertNull(newState.pieces.find { it.id == p1Soldier.id })
        assertNull(newState.pieces.find { it.id == p2Soldier.id })

        // Captures count increments
        assertEquals(1, newState.captures)
        assertEquals(1, newState.playerOneCaptures)
    }

    @Test
    fun testFlagCapture_immediateVictory() {
        val p1Soldier = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(1, 3))
        val p2Flag = GamePiece(id = 2, owner = Player.PLAYER_TWO, type = PieceType.FLAG, position = GridPosition(0, 3), revealed = false)

        val state = FlagCaptureState(
            pieces = listOf(p1Soldier, p2Flag),
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE
        )

        val outcome = GameRules.movePiece(p1Soldier.id, GridPosition(0, 3), state)
        assertTrue(outcome is GameRules.MoveOutcome.Success)

        val newState = (outcome as GameRules.MoveOutcome.Success).newState
        assertEquals(Player.PLAYER_ONE, newState.winner)
        assertEquals(WinningReason.FLAG_CAPTURED, newState.winningReason)

        val capturedFlag = newState.pieces.find { it.id == p2Flag.id }
        assertNotNull(capturedFlag)
        assertTrue(capturedFlag!!.revealed)
    }

    @Test
    fun testTurnSwitching_afterSuccessfulMove() {
        val p1Soldier = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5))
        val state = FlagCaptureState(
            pieces = listOf(p1Soldier),
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE,
            turnCount = 1
        )

        val outcome = GameRules.movePiece(p1Soldier.id, GridPosition(4, 5), state)
        assertTrue(outcome is GameRules.MoveOutcome.Success)

        val newState = (outcome as GameRules.MoveOutcome.Success).newState
        assertEquals(Player.PLAYER_TWO, newState.currentPlayer)
        assertEquals(2, newState.turnCount)
        assertNull(newState.winner)
    }

    @Test
    fun testWinnerDetection_opponentEliminated() {
        // P1 has 1 soldier, P2 has 1 soldier. P1 attacks P2 soldier -> both removed -> P2 has 0 soldiers, P1 has 0 soldiers. Attacker wins!
        val p1Soldier = GamePiece(id = 1, owner = Player.PLAYER_ONE, type = PieceType.SOLDIER, position = GridPosition(5, 5))
        val p2Soldier = GamePiece(id = 2, owner = Player.PLAYER_TWO, type = PieceType.SOLDIER, position = GridPosition(4, 5))
        val p1Flag = GamePiece(id = 3, owner = Player.PLAYER_ONE, type = PieceType.FLAG, position = GridPosition(7, 4))
        val p2Flag = GamePiece(id = 4, owner = Player.PLAYER_TWO, type = PieceType.FLAG, position = GridPosition(0, 3))

        val state = FlagCaptureState(
            pieces = listOf(p1Soldier, p2Soldier, p1Flag, p2Flag),
            blockedCells = emptySet(),
            currentPlayer = Player.PLAYER_ONE
        )

        val outcome = GameRules.movePiece(p1Soldier.id, GridPosition(4, 5), state)
        assertTrue(outcome is GameRules.MoveOutcome.Success)

        val newState = (outcome as GameRules.MoveOutcome.Success).newState
        assertEquals(Player.PLAYER_ONE, newState.winner)
        assertEquals(WinningReason.OPPONENT_DEFEATED, newState.winningReason)
    }

    @Test
    fun testUndoRestoration_restoresState() {
        val state = GameRules.createInitialState()
        val snapshot = GameRules.createSnapshot(state)

        val p1Piece = state.pieces.first { it.owner == Player.PLAYER_ONE && it.type == PieceType.SOLDIER }
        val moves = GameRules.getLegalMoves(p1Piece, state)
        assertTrue(moves.isNotEmpty())

        val target = moves.first()
        val outcome = GameRules.movePiece(p1Piece.id, target, state)
        assertTrue(outcome is GameRules.MoveOutcome.Success)

        val movedState = (outcome as GameRules.MoveOutcome.Success).newState
        assertEquals(Player.PLAYER_TWO, movedState.currentPlayer)
        assertEquals(2, movedState.turnCount)

        // Restore snapshot
        val restoredState = GameRules.restoreSnapshot(snapshot, state.blockedCells)
        assertEquals(Player.PLAYER_ONE, restoredState.currentPlayer)
        assertEquals(1, restoredState.turnCount)
        assertEquals(state.pieces.size, restoredState.pieces.size)
        assertEquals(p1Piece.position, restoredState.pieces.first { it.id == p1Piece.id }.position)
    }

    @Test
    fun testRestartRestoration_resetsToOriginal() {
        val state = GameRules.createInitialState()
        val p1Piece = state.pieces.first { it.owner == Player.PLAYER_ONE && it.type == PieceType.SOLDIER }
        val target = GameRules.getLegalMoves(p1Piece, state).first()

        val movedState = (GameRules.movePiece(p1Piece.id, target, state) as GameRules.MoveOutcome.Success).newState
        assertEquals(2, movedState.turnCount)

        // Restarting
        val freshState = GameRules.createInitialState()
        assertEquals(1, freshState.turnCount)
        assertEquals(Player.PLAYER_ONE, freshState.currentPlayer)
        assertEquals(12, freshState.pieces.size)
        assertEquals(0, freshState.captures)
    }
}
