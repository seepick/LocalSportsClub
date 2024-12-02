package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import seepick.localsportsclub.logic.logicModule

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(logicModule)
    }) {
        Buttons()
    }
}