package com.example.flagcapture.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.flagcapture.data.GamePreferencesRepository
import com.example.flagcapture.model.AppThemeMode
import com.example.flagcapture.model.BoardThemeMode
import com.example.flagcapture.model.GameSettings
import com.example.flagcapture.model.GameStatistics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamePreferencesRepository(application.applicationContext)

    val settings: StateFlow<GameSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameSettings()
    )

    val statistics: StateFlow<GameStatistics> = repository.statisticsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GameStatistics()
    )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.updateThemeMode(mode)
        }
    }

    fun setBoardTheme(boardTheme: BoardThemeMode) {
        viewModelScope.launch {
            repository.updateBoardTheme(boardTheme)
        }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSoundEffects(enabled)
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateHapticFeedback(enabled)
        }
    }

    fun setShowLegalMoves(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateShowLegalMoves(enabled)
        }
    }

    fun setFastAnimations(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateFastAnimations(enabled)
        }
    }

    fun resetStatistics() {
        viewModelScope.launch {
            repository.resetStatistics()
        }
    }
}
