package com.example.flagcapture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.flagcapture.ui.screens.AboutScreen
import com.example.flagcapture.ui.screens.GameScreen
import com.example.flagcapture.ui.screens.GameViewModel
import com.example.flagcapture.ui.screens.HomeScreen
import com.example.flagcapture.ui.screens.HowToPlayScreen
import com.example.flagcapture.ui.screens.ResultScreen
import com.example.flagcapture.ui.screens.SettingsScreen
import com.example.flagcapture.ui.screens.SettingsViewModel
import com.example.flagcapture.ui.screens.SplashScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    settingsViewModel: SettingsViewModel = viewModel(),
    gameViewModel: GameViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                settingsViewModel = settingsViewModel,
                onPlayClick = {
                    gameViewModel.restartGame()
                    navController.navigate(Screen.Game.route)
                },
                onHowToPlayClick = {
                    navController.navigate(Screen.HowToPlay.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                gameViewModel = gameViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { winner, reason, captures, turns, p1Rem, p2Rem, duration ->
                    val route = Screen.Result.createRoute(winner, reason, captures, turns, p1Rem, p2Rem, duration)
                    navController.navigate(route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                },
                onHowToPlayClick = {
                    navController.navigate(Screen.HowToPlay.route)
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("winner") { type = NavType.StringType },
                navArgument("reason") { type = NavType.StringType },
                navArgument("captures") { type = NavType.IntType },
                navArgument("turns") { type = NavType.IntType },
                navArgument("p1Remaining") { type = NavType.IntType },
                navArgument("p2Remaining") { type = NavType.IntType },
                navArgument("durationSeconds") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner") ?: "PLAYER_ONE"
            val reason = backStackEntry.arguments?.getString("reason") ?: "FLAG_CAPTURED"
            val captures = backStackEntry.arguments?.getInt("captures") ?: 0
            val turns = backStackEntry.arguments?.getInt("turns") ?: 1
            val p1Remaining = backStackEntry.arguments?.getInt("p1Remaining") ?: 0
            val p2Remaining = backStackEntry.arguments?.getInt("p2Remaining") ?: 0
            val durationSeconds = backStackEntry.arguments?.getLong("durationSeconds") ?: 0L

            ResultScreen(
                winnerStr = winner,
                reasonStr = reason,
                captures = captures,
                turns = turns,
                p1Remaining = p1Remaining,
                p2Remaining = p2Remaining,
                durationSeconds = durationSeconds,
                onPlayAgainClick = {
                    gameViewModel.restartGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HowToPlay.route) {
            HowToPlayScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
