package seepick.localsportsclub

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import seepick.localsportsclub.view.App

fun main() = application {
    val config = if (Environment.current == Environment.Development) AppConfig.development else AppConfig.production

    Window(
        onCloseRequest = ::exitApplication,
        title = "LocalSportsClub",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        App(config)
    }
}
