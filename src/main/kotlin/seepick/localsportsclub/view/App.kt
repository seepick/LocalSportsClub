package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.logic.logicModule
import seepick.localsportsclub.persistence.persistenceModule

@Composable
@Preview
fun App(config: AppConfig) {
    KoinApplication(application = {
        modules(
            persistenceModule(config),
            logicModule()
        )
    }) {
        Buttons()
    }
}