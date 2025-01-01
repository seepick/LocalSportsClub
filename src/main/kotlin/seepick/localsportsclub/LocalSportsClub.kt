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
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.MainView
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.SyncerViewModel
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.notes.NotesViewModel
import seepick.localsportsclub.view.usage.UsageStorage
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
        val log = logger {}
        log.info { "Starting up application for environment: ${Environment.current.name}" }
        application {
            KoinApplication(application = {
                modules(allModules(config))
            }) {
                val keyboard: GlobalKeyboard = koinInject()
                val applicationLifecycle: ApplicationLifecycle = koinInject()
                applicationLifecycle.attachMacosQuitHandler() // when CMD+Q is executed (or from menubar)
                Window(
                    title = "LocalSportsClub${if (Environment.current == Environment.Development) " - DEV 🤓" else ""}",
                    state = rememberWindowState(
                        width = 1_500.dp, height = 1200.dp,
                        position = WindowPosition(100.dp, 100.dp),
                    ),
                    onKeyEvent = { keyboard.process(it); false },
                    onCloseRequest = {
                        applicationLifecycle.onExit() // when window close button is clicked
                        exitApplication()
                    },
                ) {
                    val syncer = koinInject<Syncer>()
                    val dataStorage = koinInject<DataStorage>()
                    val usage = koinInject<UsageStorage>()
                    syncer.registerListener(dataStorage)
                    syncer.registerListener(usage)
                    dataStorage.registerListener(koinViewModel<SyncerViewModel>())
                    dataStorage.registerListener(koinViewModel<ActivityViewModel>())
                    dataStorage.registerListener(koinViewModel<FreetrainingViewModel>())
                    dataStorage.registerListener(koinViewModel<VenueViewModel>())

                    applicationLifecycle.registerListener(usage)
                    applicationLifecycle.registerListener(koinViewModel<ActivityViewModel>())
                    applicationLifecycle.registerListener(koinViewModel<FreetrainingViewModel>())
                    applicationLifecycle.registerListener(koinViewModel<VenueViewModel>())
                    applicationLifecycle.registerListener(koinViewModel<NotesViewModel>())

                    window.addWindowListener(object : WindowAdapter() {
                        // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
                        override fun windowOpened(e: WindowEvent?) {
                            applicationLifecycle.onStartUp()
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
