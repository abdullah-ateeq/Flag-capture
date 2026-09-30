package com.example.flagcapture.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.flagcapture.model.AppThemeMode
import com.example.flagcapture.model.BoardThemeMode
import com.example.flagcapture.model.GameSettings
import com.example.flagcapture.model.GameStatistics
import com.example.flagcapture.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flag_capture_prefs")

class GamePreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SHOW_LEGAL_MOVES = booleanPreferencesKey("show_legal_moves")
        val FAST_ANIMATIONS = booleanPreferencesKey("fast_animations")

        val GAMES_PLAYED = intPreferencesKey("games_played")
        val PLAYER_ONE_WINS = intPreferencesKey("player_one_wins")
        val PLAYER_TWO_WINS = intPreferencesKey("player_two_wins")
        val TOTAL_CAPTURES = intPreferencesKey("total_captures")
        val FASTEST_VICTORY_TURNS = intPreferencesKey("fastest_victory_turns")
        val FASTEST_VICTORY_SECONDS = longPreferencesKey("fastest_victory_seconds")
    }

    val settingsFlow: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        val themeModeStr = prefs[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val boardThemeStr = prefs[PreferencesKeys.BOARD_THEME] ?: BoardThemeMode.CLASSIC.name
        val soundEffects = prefs[PreferencesKeys.SOUND_EFFECTS] ?: true
        val hapticFeedback = prefs[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        val showLegalMoves = prefs[PreferencesKeys.SHOW_LEGAL_MOVES] ?: true
        val fastAnimations = prefs[PreferencesKeys.FAST_ANIMATIONS] ?: false

        GameSettings(
            themeMode = runCatching { AppThemeMode.valueOf(themeModeStr) }.getOrDefault(AppThemeMode.SYSTEM),
            boardTheme = runCatching { BoardThemeMode.valueOf(boardThemeStr) }.getOrDefault(BoardThemeMode.CLASSIC),
            soundEffects = soundEffects,
            hapticFeedback = hapticFeedback,
            showLegalMoves = showLegalMoves,
            fastAnimations = fastAnimations
        )
    }

    val statisticsFlow: Flow<GameStatistics> = context.dataStore.data.map { prefs ->
        GameStatistics(
            gamesPlayed = prefs[PreferencesKeys.GAMES_PLAYED] ?: 0,
            playerOneWins = prefs[PreferencesKeys.PLAYER_ONE_WINS] ?: 0,
            playerTwoWins = prefs[PreferencesKeys.PLAYER_TWO_WINS] ?: 0,
            totalCaptures = prefs[PreferencesKeys.TOTAL_CAPTURES] ?: 0,
            fastestVictoryTurns = prefs[PreferencesKeys.FASTEST_VICTORY_TURNS] ?: 0,
            fastestVictorySeconds = prefs[PreferencesKeys.FASTEST_VICTORY_SECONDS] ?: 0L
        )
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateBoardTheme(boardTheme: BoardThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BOARD_THEME] = boardTheme.name
        }
    }

    suspend fun updateSoundEffects(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUND_EFFECTS] = enabled
        }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun updateShowLegalMoves(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SHOW_LEGAL_MOVES] = enabled
        }
    }

    suspend fun updateFastAnimations(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FAST_ANIMATIONS] = enabled
        }
    }

    suspend fun recordGameResult(winner: Player, turns: Int, durationSeconds: Long, capturesInMatch: Int) {
        context.dataStore.edit { prefs ->
            val games = (prefs[PreferencesKeys.GAMES_PLAYED] ?: 0) + 1
            prefs[PreferencesKeys.GAMES_PLAYED] = games

            if (winner == Player.PLAYER_ONE) {
                prefs[PreferencesKeys.PLAYER_ONE_WINS] = (prefs[PreferencesKeys.PLAYER_ONE_WINS] ?: 0) + 1
            } else {
                prefs[PreferencesKeys.PLAYER_TWO_WINS] = (prefs[PreferencesKeys.PLAYER_TWO_WINS] ?: 0) + 1
            }

            prefs[PreferencesKeys.TOTAL_CAPTURES] = (prefs[PreferencesKeys.TOTAL_CAPTURES] ?: 0) + capturesInMatch

            val currentFastestTurns = prefs[PreferencesKeys.FASTEST_VICTORY_TURNS] ?: 0
            if (currentFastestTurns == 0 || turns < currentFastestTurns) {
                prefs[PreferencesKeys.FASTEST_VICTORY_TURNS] = turns
            }

            val currentFastestSeconds = prefs[PreferencesKeys.FASTEST_VICTORY_SECONDS] ?: 0L
            if (currentFastestSeconds == 0L || durationSeconds < currentFastestSeconds) {
                prefs[PreferencesKeys.FASTEST_VICTORY_SECONDS] = durationSeconds
            }
        }
    }

    suspend fun resetStatistics() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GAMES_PLAYED] = 0
            prefs[PreferencesKeys.PLAYER_ONE_WINS] = 0
            prefs[PreferencesKeys.PLAYER_TWO_WINS] = 0
            prefs[PreferencesKeys.TOTAL_CAPTURES] = 0
            prefs[PreferencesKeys.FASTEST_VICTORY_TURNS] = 0
            prefs[PreferencesKeys.FASTEST_VICTORY_SECONDS] = 0L
        }
    }
}
