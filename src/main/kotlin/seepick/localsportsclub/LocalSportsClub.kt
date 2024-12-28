package seepick.localsportsclub

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ch.qos.logback.classic.Level
import seepick.localsportsclub.view.ComposeApp

class GlobalKeyboard {
    fun changeToScreen(screenNr: Int) {

    }
}

object LocalSportsClub {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = if (Environment.current == Environment.Development) {
            AppConfig.development
        } else {
            AppConfig.production
        }
        reconfigureLog(
            useFileAppender = config.logFileEnabled,
            packageSettings = mapOf(
                "seepick.localsportsclub" to Level.TRACE,
                "liquibase" to Level.INFO,
                "Exposed" to Level.INFO,
            )
        )
        application {
            val globalKeyboard = GlobalKeyboard()
            Window(
                onCloseRequest = {
                    println("close requested...")
                    // TODO save notes
                    exitApplication()
                },
                title = "LocalSportsClub",
                state = rememberWindowState(
                    width = 1_500.dp, height = 1200.dp,
                    position = WindowPosition(100.dp, 100.dp),
                ),
                onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyDown && event.isMetaPressed) {
                        when (event.key) {
                            Key.One -> globalKeyboard.changeToScreen(1)
                            Key.Two -> globalKeyboard.changeToScreen(2)
                            Key.Three -> globalKeyboard.changeToScreen(3)
                        }
                    }
                    false
                },
            ) {
                LscTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background,
                    ) {
                        ComposeApp(window, config, globalKeyboard)
                    }
                }
            }
        }
    }
}
