package seepick.localsportsclub.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.GlobalKeyboard
import seepick.localsportsclub.allModules
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.venue.VenueViewModel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

@Composable
fun ComposeApp(window: ComposeWindow, config: AppConfig, keyboard: GlobalKeyboard) {
    KoinApplication(application = {
        modules(allModules(config))
    }) {
        val syncer = koinInject<Syncer>()
        val dataStorage = koinInject<DataStorage>()
        syncer.registerListener(dataStorage)
        dataStorage.registerListener(koinViewModel<VenueViewModel>())
        dataStorage.registerListener(koinViewModel<ActivityViewModel>())

        val venueViewModel = koinViewModel<VenueViewModel>()
        val activityViewModel = koinViewModel<ActivityViewModel>()
        window.addWindowListener(object : WindowAdapter() {
            // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
            override fun windowOpened(e: WindowEvent?) {
                venueViewModel.onStartUp()
                activityViewModel.onStartUp()
            }
        })

        MainView(keyboard)
    }
}
