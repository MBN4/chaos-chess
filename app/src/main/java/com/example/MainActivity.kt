package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.chaoschess.ai.AIDifficulty
import com.example.chaoschess.audio.SoundEngine
import com.example.chaoschess.data.GameSettings
import com.example.chaoschess.data.SettingsRepository
import com.example.chaoschess.engine.models.CustomChaosConfig
import com.example.chaoschess.engine.models.GameMode
import com.example.chaoschess.engine.models.PieceColor
import com.example.chaoschess.ui.screens.CustomChaosBuilderScreen
import com.example.chaoschess.ui.screens.GameScreen
import com.example.chaoschess.ui.screens.MainMenuScreen
import com.example.chaoschess.ui.screens.MatchOpponentType
import com.example.chaoschess.ui.screens.MatchSetupScreen
import com.example.chaoschess.ui.screens.ModeSelectScreen
import com.example.chaoschess.ui.screens.ReplayScreen
import com.example.chaoschess.ui.screens.SplashScreen
import com.example.chaoschess.ui.screens.TutorialScreen
import com.example.chaoschess.ui.screens.SettingsScreen
import com.example.ui.theme.ChaosChessTheme

enum class AppScreen {
    SPLASH,
    MAIN_MENU,
    MODE_SELECT,
    MATCH_SETUP,
    GAME,
    CUSTOM_CHAOS,
    TUTORIAL,
    SETTINGS,
    REPLAYS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SettingsRepository(applicationContext)
        val initialSettings = repository.loadSettings()
        SoundEngine.soundEnabled = initialSettings.soundEnabled
        SoundEngine.sfxVolume = initialSettings.sfxVolume

        setContent {
            ChaosChessTheme {
                ChaosChessApp(repository = repository, initialSettings = initialSettings)
            }
        }
    }
}

@Composable
fun ChaosChessApp(
    repository: SettingsRepository,
    initialSettings: GameSettings
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
    var customConfig by remember { mutableStateOf<CustomChaosConfig?>(null) }
    var opponentType by remember { mutableStateOf(MatchOpponentType.AI) }
    var aiDifficulty by remember { mutableStateOf(AIDifficulty.MEDIUM) }
    var playerColor by remember { mutableStateOf(PieceColor.WHITE) }
    var settings by remember { mutableStateOf(initialSettings) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                AppScreen.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.MAIN_MENU -> {
                    MainMenuScreen(
                        onPlayClassic = {
                            selectedMode = GameMode.CLASSIC
                            customConfig = null
                            currentScreen = AppScreen.MATCH_SETUP
                        },
                        onGameModes = {
                            currentScreen = AppScreen.MODE_SELECT
                        },
                        onCustomChaos = {
                            currentScreen = AppScreen.CUSTOM_CHAOS
                        },
                        onHowToPlay = {
                            currentScreen = AppScreen.TUTORIAL
                        },
                        onReplays = {
                            currentScreen = AppScreen.REPLAYS
                        },
                        onSettings = {
                            currentScreen = AppScreen.SETTINGS
                        }
                    )
                }
                AppScreen.MODE_SELECT -> {
                    ModeSelectScreen(
                        onModeSelected = { mode ->
                            selectedMode = mode
                            if (mode == GameMode.CUSTOM_CHAOS) {
                                currentScreen = AppScreen.CUSTOM_CHAOS
                            } else {
                                customConfig = null
                                currentScreen = AppScreen.MATCH_SETUP
                            }
                        },
                        onBackClicked = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.MATCH_SETUP -> {
                    MatchSetupScreen(
                        mode = selectedMode,
                        onStartMatch = { opp, diff, col ->
                            opponentType = opp
                            aiDifficulty = diff
                            playerColor = col
                            currentScreen = AppScreen.GAME
                        },
                        onBackClicked = { currentScreen = AppScreen.MODE_SELECT }
                    )
                }
                AppScreen.GAME -> {
                    GameScreen(
                        mode = selectedMode,
                        customConfig = customConfig,
                        opponentType = opponentType,
                        aiDifficulty = aiDifficulty,
                        playerColor = playerColor,
                        settings = settings,
                        repository = repository,
                        onNavigateBackToMenu = { currentScreen = AppScreen.MAIN_MENU },
                        onNavigateToModeSelect = { currentScreen = AppScreen.MODE_SELECT },
                        onNavigateToTutorial = { currentScreen = AppScreen.TUTORIAL },
                        onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
                    )
                }
                AppScreen.CUSTOM_CHAOS -> {
                    CustomChaosBuilderScreen(
                        onStartCustomMatch = { cfg ->
                            selectedMode = GameMode.CUSTOM_CHAOS
                            customConfig = cfg
                            currentScreen = AppScreen.MATCH_SETUP
                        },
                        onBackClicked = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.TUTORIAL -> {
                    TutorialScreen(
                        onBackClicked = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentSettings = settings,
                        repository = repository,
                        onSettingsChanged = { settings = it },
                        onBackClicked = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
                AppScreen.REPLAYS -> {
                    ReplayScreen(
                        repository = repository,
                        onBackClicked = { currentScreen = AppScreen.MAIN_MENU }
                    )
                }
            }
        }
    }
}
