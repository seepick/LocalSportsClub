package seepick.localsportsclub

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import seepick.localsportsclub.view.ComposeApp

object LocalSportsClub {
    @JvmStatic
    fun main(args: Array<String>) = application {
        val config = if (Environment.current == Environment.Development) {
            AppConfig.development
        } else {
            AppConfig.production
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "LocalSportsClub",
            state = rememberWindowState(
                width = 1_000.dp, height = 600.dp,
                position = WindowPosition(100.dp, 100.dp),
            )
        ) {
            ComposeApp(window, config)
        }
    }
}
