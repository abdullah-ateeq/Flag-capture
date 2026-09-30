package com.example.flagcapture.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Game : Screen("game")
    object Result : Screen("result/{winner}/{reason}/{captures}/{turns}/{p1Remaining}/{p2Remaining}/{durationSeconds}") {
        fun createRoute(
            winner: String,
            reason: String,
            captures: Int,
            turns: Int,
            p1Remaining: Int,
            p2Remaining: Int,
            durationSeconds: Long
        ): String {
            return "result/$winner/$reason/$captures/$turns/$p1Remaining/$p2Remaining/$durationSeconds"
        }
    }
    object HowToPlay : Screen("how_to_play")
    object Settings : Screen("settings")
    object About : Screen("about")
}
