# 🚩 Flag Capture - Android Board Game

A modern, tactical two-player strategy board game built natively for Android using **Jetpack Compose** and **Material Design 3**. 

---

## 🎮 About The Game

**Flag Capture** is an 8x8 turn-based strategy game where two players face off on a grid filled with obstacles, neutral corridor zones, and tactical options. Each player commands a force consisting of 5 Soldiers and 1 Flag.

### 🎯 Objective
To win the match, you must either:
1. **Capture the Opponent's Flag** by moving one of your Soldiers onto the square containing their Flag.
2. **Eliminate All Opponent Soldiers** through strategic combat encounters.

---

## ✨ Features

- **⚔️ Strategic 2-Player Gameplay**:
  - Symmetric initial setup on an 8x8 grid with neutral zone obstacles.
  - Orthogonal 1-step movement mechanics.
  - Dynamic combat resolution:
    - **Soldier vs. Soldier**: Both pieces are eliminated from the board!
    - **Soldier vs. Flag**: Attacker captures the flag and wins immediately!
  - Legal move highlighting and move history with **Undo** support.
- **🎨 Modern Jetpack Compose UI**:
  - Fully declarative UI with smooth animations, custom board renderers, and HUD components.
  - Edge-to-edge layout support (`enableEdgeToEdge`).
- **🌙 Dynamic Customization & Themes**:
  - **App Theme**: System Default, Light Mode, Dark Mode.
  - **Board Themes**: Classic Wood & Modern Midnight themes.
  - **Legal Move Visualizer**: Toggle legal destination highlights on/off.
- **🔊 Audio & Haptics**:
  - Interactive sound effects for selection, movement, captures, victory, and undo.
  - Haptic feedback on piece actions.
- **📊 Persisted Preferences & Stats**:
  - Integrated Jetpack **DataStore Preferences** to save user settings and track game statistics (games played, win records, total captures, fastest victory).
- **🧪 Unit Tested Rules**:
  - Core game logic decoupled in pure Kotlin data models with thorough unit test coverage (`FlagCaptureGameRulesTest`).

---

## 📱 App Screens

1. **Splash Screen**: Animated logo transition into the main menu.
2. **Home Screen**: Quick navigation to Start Game, How to Play, Settings, and About.
3. **Game Board Screen**:
   - Interactive 8x8 Grid with blocked corridor cells.
   - Live turn counter, captured pieces HUD, and match timer.
   - Action controls: Select, Move, Deselect, Undo, and Pause/Quit menu.
4. **Result Screen**: Victory announcement with match details (Winner, Reason, Duration, Turns, Captures) and Rematch / Main Menu actions.
5. **How to Play Screen**: Clear breakdown of rules, movement, combat mechanics, and winning conditions.
6. **Settings Screen**: Customization options for themes, board styles, sound, haptics, and animations, plus lifetime stats viewer.
7. **About Screen**: Information about the game, version, and developer details.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) 2.2.10
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (UDF) using Kotlin `StateFlow`
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Data Persistence**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
- **Audio & Vibrator**: Android SoundPool & Vibrator / HapticFeedback APIs
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) & Kotlin DSL

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or standard modern release.
- **JDK**: Version 17 or higher.
- **Android SDK**:
  - `compileSdk`: 35
  - `minSdk`: 24 (Android 7.0 Nougat)

### Building & Running

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Flag-capture.git
   cd Flag-capture
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

4. **Install on Device/Emulator**:
   Connect an Android device or launch an emulator, then run:
   ```bash
   ./gradlew installDebug
   ```

---

## 📁 Project Structure

```
com.example.flagcapture/
├── audio/            # SoundManager and HapticManager implementations
├── data/             # GamePreferencesRepository (DataStore)
├── game/             # Pure game logic and state resolution (GameRules)
├── model/            # Data models (GamePiece, Player, FlagCaptureState, Settings)
├── ui/
│   ├── components/   # Modular Compose components (Board, HUD, Dialogs)
│   ├── navigation/   # NavHost setup and screen routes
│   ├── screens/      # Composable screens (Home, Game, Settings, HowToPlay, About, Result, Splash)
│   └── theme/        # Material 3 colors, typography, shapes, and themes
└── MainActivity.kt   # Main activity launching edge-to-edge compose flow
```

---

## 📜 License

This project is open source and available under the [MIT License](LICENSE).
