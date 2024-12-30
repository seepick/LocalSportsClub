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
import ch.qos.logback.classic.Level
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.MainView
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.venue.VenueViewModel
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

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
            KoinApplication(application = {
                modules(allModules(config))
            }) {
                val keyboard: GlobalKeyboard = koinInject()
                Window(
                    onCloseRequest = { exitApplication() }, // TODO save notes
                    title = "LocalSportsClub",
                    state = rememberWindowState(
                        width = 1_500.dp, height = 1200.dp,
                        position = WindowPosition(100.dp, 100.dp),
                    ),
                    onKeyEvent = { keyboard.process(it); false },
                ) {
                    val syncer = koinInject<Syncer>()
                    val dataStorage = koinInject<DataStorage>()
                    syncer.registerListener(dataStorage)
                    dataStorage.registerListener(koinViewModel<VenueViewModel>())
                    dataStorage.registerListener(koinViewModel<ActivityViewModel>())
                    dataStorage.registerListener(koinViewModel<FreetrainingViewModel>())

                    val venueViewModel = koinViewModel<VenueViewModel>()
                    val activityViewModel = koinViewModel<ActivityViewModel>()
                    val freetrainingViewModel = koinViewModel<FreetrainingViewModel>()
                    window.addWindowListener(object : WindowAdapter() {
                        // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
                        override fun windowOpened(e: WindowEvent?) {
                            venueViewModel.onStartUp()
                            activityViewModel.onStartUp()
                            freetrainingViewModel.onStartUp()
                        }
                    })

                    val mainViewModel = koinViewModel<MainViewModel>()
                    keyboard.registerListener(mainViewModel)

                    LscTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colors.background,
                        ) {
                            MainView()
                        }
                    }
                }
            }
        }
    }
}
