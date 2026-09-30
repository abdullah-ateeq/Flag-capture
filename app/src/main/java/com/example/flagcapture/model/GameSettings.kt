package com.example.flagcapture.model

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class BoardThemeMode {
    CLASSIC,
    MIDNIGHT
}

data class GameSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val boardTheme: BoardThemeMode = BoardThemeMode.CLASSIC,
    val soundEffects: Boolean = true,
    val hapticFeedback: Boolean = true,
    val showLegalMoves: Boolean = true,
    val fastAnimations: Boolean = false
)

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val playerOneWins: Int = 0,
    val playerTwoWins: Int = 0,
    val totalCaptures: Int = 0,
    val fastestVictoryTurns: Int = 0,
    val fastestVictorySeconds: Long = 0L
)
