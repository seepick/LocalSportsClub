package seepick.localsportsclub.view

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.allModules
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.venue.VenueViewModel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Composable
@Preview
fun ComposeApp(window: ComposeWindow, config: AppConfig) {
    KoinApplication(application = {
        modules(allModules(config))
    }) {
        val venueViewModel = koinViewModel<VenueViewModel>()
        val syncer = koinInject<Syncer>()
        val dataStorage = koinInject<DataStorage>()
        syncer.registerListener(dataStorage)
        dataStorage.registerListener(venueViewModel)

        window.addWindowListener(object : WindowAdapter() {
            // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
            override fun windowOpened(e: WindowEvent?) {
                // maybe could also have done a LaunchedEffect(true) hack...?
                venueViewModel.onStartUp()
            }
        })

        MainWindow()
    }
}
