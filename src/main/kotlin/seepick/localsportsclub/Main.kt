package seepick.localsportsclub

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.view.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LocalSportsClub",
        state = rememberWindowState(width = 300.dp, height = 300.dp)
    ) {
        App()
    }
}
