package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.allModules
import seepick.localsportsclub.sync.SyncDispatcher
import seepick.localsportsclub.view.venue.VenueViewModel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Composable
@Preview
fun ComposeApp(window: ComposeWindow, config: AppConfig) {
    KoinApplication(application = {
        modules(allModules(config))
    }) {
        MainWindow()

        val venueVM = koinViewModel<VenueViewModel>()
        val syncDispatcher = koinInject<SyncDispatcher>()
        syncDispatcher.registerOnVenueAdded(venueVM::onVenueAdded)

        window.addWindowListener(object : WindowAdapter() {
            // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
            override fun windowOpened(e: WindowEvent?) {
                venueVM.onStartUp()
            }
        })
    }
}
