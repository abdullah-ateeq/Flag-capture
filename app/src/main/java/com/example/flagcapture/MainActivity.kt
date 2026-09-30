package com.example.flagcapture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.flagcapture.ui.navigation.AppNavigation
import com.example.flagcapture.ui.screens.GameViewModel
import com.example.flagcapture.ui.screens.SettingsViewModel
import com.example.flagcapture.ui.theme.FlagCaptureTheme

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            FlagCaptureTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    AppNavigation(
                        settingsViewModel = settingsViewModel,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}