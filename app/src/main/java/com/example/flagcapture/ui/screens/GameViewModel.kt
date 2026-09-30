package com.example.flagcapture.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flagcapture.audio.HapticManager
import com.example.flagcapture.audio.SoundManager
import com.example.flagcapture.data.GamePreferencesRepository
import com.example.flagcapture.game.GameRules
import com.example.flagcapture.model.CombatEvent
import com.example.flagcapture.model.FlagCaptureState
import com.example.flagcapture.model.GamePiece
import com.example.flagcapture.model.GameSettings
import com.example.flagcapture.model.GameStateSnapshot
import com.example.flagcapture.model.GridPosition
import com.example.flagcapture.model.PieceType
import com.example.flagcapture.model.Player
import com.example.flagcapture.model.WinningReason
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameResultData(
    val winner: Player,
    val reason: WinningReason,
    val captures: Int,
    val turns: Int,
    val p1Remaining: Int,
    val p2Remaining: Int,
    val durationSeconds: Long
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)
    private val soundManager = SoundManager()
    private val hapticManager = HapticManager(application.applicationContext)

    val settings: StateFlow<GameSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameSettings()
    )

    private val _gameState = MutableStateFlow(GameRules.createInitialState())
    val gameState: StateFlow<FlagCaptureState> = _gameState.asStateFlow()

    private val _gameResultEvent = MutableSharedFlow<GameResultData>()
    val gameResultEvent: SharedFlow<GameResultData> = _gameResultEvent.asSharedFlow()

    private val undoStack = mutableListOf<GameStateSnapshot>()
    private var timerJob: Job? = null

    init {
        startMatchTimer()
    }

    private fun startMatchTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_gameState.value.winner == null) {
                    _gameState.update { current ->
                        val duration = (System.currentTimeMillis() - current.matchStartTimeMs) / 1000L
                        current.copy(matchDurationSeconds = duration.coerceAtLeast(0L))
                    }
                }
            }
        }
    }

    fun onCellClicked(position: GridPosition) {
        val current = _gameState.value
        if (current.winner != null || current.isAnimating) return

        val selectedPiece = current.selectedPieceId?.let { id -> current.pieces.find { it.id == id } }

        if (selectedPiece != null) {
            // Check if clicking the same piece to deselect
            if (selectedPiece.position == position) {
                _gameState.update { it.copy(selectedPieceId = null, legalMoves = emptySet()) }
                return
            }

            // Check if clicking another friendly piece
            val friendlyTarget = current.pieces.find { it.position == position && it.owner == current.currentPlayer }
            if (friendlyTarget != null) {
                if (friendlyTarget.type == PieceType.SOLDIER) {
                    val moves = if (settings.value.showLegalMoves) {
                        GameRules.getLegalMoves(friendlyTarget, current)
                    } else emptySet()
                    _gameState.update { it.copy(selectedPieceId = friendlyTarget.id, legalMoves = moves) }
                } else {
                    // Flags cannot move
                    _gameState.update { it.copy(selectedPieceId = friendlyTarget.id, legalMoves = emptySet()) }
                }
                return
            }

            // Check if clicking a legal move destination
            val legalMoves = GameRules.getLegalMoves(selectedPiece, current)
            if (position in legalMoves) {
                executeMove(selectedPiece.id, position)
            } else {
                // Clicked an invalid destination, clear selection
                _gameState.update { it.copy(selectedPieceId = null, legalMoves = emptySet()) }
            }
        } else {
            // No piece is currently selected, check if user tapped a friendly piece
            val tappedPiece = current.pieces.find { it.position == position && it.owner == current.currentPlayer }
            if (tappedPiece != null) {
                val moves = if (tappedPiece.type == PieceType.SOLDIER && settings.value.showLegalMoves) {
                    GameRules.getLegalMoves(tappedPiece, current)
                } else emptySet()
                _gameState.update { it.copy(selectedPieceId = tappedPiece.id, legalMoves = moves) }
            }
        }
    }

    private fun executeMove(pieceId: Int, destination: GridPosition) {
        val current = _gameState.value
        if (current.winner != null || current.isAnimating) return

        // Push snapshot onto undo stack before move
        undoStack.add(GameRules.createSnapshot(current))

        // Trigger animation state
        _gameState.update { it.copy(isAnimating = true) }

        viewModelScope.launch {
            val animDuration = if (settings.value.fastAnimations) 90L else 180L
            delay(animDuration)

            when (val result = GameRules.movePiece(pieceId, destination, current)) {
                is GameRules.MoveOutcome.Success -> {
                    val updatedState = result.newState.copy(
                        isAnimating = false,
                        undoAvailable = undoStack.isNotEmpty()
                    )
                    _gameState.value = updatedState

                    handleMoveFeedback(result.combatEvent, updatedState)

                    if (updatedState.winner != null && updatedState.winningReason != null) {
                        handleVictory(updatedState)
                    }
                }
                is GameRules.MoveOutcome.Invalid -> {
                    // Revert animation state
                    _gameState.update { it.copy(isAnimating = false) }
                }
            }
        }
    }

    private fun handleMoveFeedback(combatEvent: CombatEvent?, state: FlagCaptureState) {
        val soundOn = settings.value.soundEffects
        val hapticOn = settings.value.hapticFeedback

        if (combatEvent != null) {
            if (combatEvent.isFlagCapture) {
                if (soundOn) soundManager.playFlagDiscovered()
                if (hapticOn) hapticManager.vibrateFlagCapture()
            } else {
                if (soundOn) soundManager.playCapture()
                if (hapticOn) hapticManager.vibrateCapture()
            }
        } else {
            if (soundOn) soundManager.playMove()
            if (hapticOn) hapticManager.vibrateMove()
        }
    }

    private fun handleVictory(state: FlagCaptureState) {
        val winner = state.winner ?: return
        val reason = state.winningReason ?: WinningReason.FLAG_CAPTURED

        if (settings.value.soundEffects) soundManager.playVictory()
        if (settings.value.hapticFeedback) hapticManager.vibrateFlagCapture()

        val p1Remaining = state.pieces.count { it.owner == Player.PLAYER_ONE }
        val p2Remaining = state.pieces.count { it.owner == Player.PLAYER_TWO }
        val duration = (System.currentTimeMillis() - state.matchStartTimeMs) / 1000L

        viewModelScope.launch {
            repository.recordGameResult(
                winner = winner,
                turns = state.turnCount,
                durationSeconds = duration,
                capturesInMatch = state.captures
            )

            // Allow brief delay so players see the final board state
            delay(1200)
            _gameResultEvent.emit(
                GameResultData(
                    winner = winner,
                    reason = reason,
                    captures = state.captures,
                    turns = state.turnCount,
                    p1Remaining = p1Remaining,
                    p2Remaining = p2Remaining,
                    durationSeconds = duration
                )
            )
        }
    }

    fun undoMove() {
        val current = _gameState.value
        if (current.isAnimating || undoStack.isEmpty()) return

        val previousSnapshot = undoStack.removeAt(undoStack.lastIndex)
        val restoredState = GameRules.restoreSnapshot(previousSnapshot, current.blockedCells).copy(
            undoAvailable = undoStack.isNotEmpty(),
            matchStartTimeMs = current.matchStartTimeMs,
            matchDurationSeconds = current.matchDurationSeconds
        )

        _gameState.value = restoredState

        if (settings.value.soundEffects) soundManager.playMove()
        if (settings.value.hapticFeedback) hapticManager.vibrateMove()
    }

    fun restartGame() {
        undoStack.clear()
        _gameState.value = GameRules.createInitialState()
        startMatchTimer()
    }
}
