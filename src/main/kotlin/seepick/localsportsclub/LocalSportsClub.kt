package seepick.localsportsclub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
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
            onCloseRequest = {
                println("close requested...")
                exitApplication()
            }, title = "LocalSportsClub", state = rememberWindowState(
                width = 1_500.dp, height = 1200.dp,
                position = WindowPosition(100.dp, 100.dp),
            )
        ) {
            LscTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                ) {
                    ComposeApp(window, config)
                }
            }
        }
    }
}
