package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.allModules

@Composable
@Preview
fun App(config: AppConfig) {
    KoinApplication(application = {
        modules(allModules(config))
    }) {
        MainWindow()
    }
}
