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
import seepick.localsportsclub.service.BookingService
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.sync.SyncProgress
import seepick.localsportsclub.sync.SyncReporter
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.view.MainView
import seepick.localsportsclub.view.MainViewModel
import seepick.localsportsclub.view.Screen
import seepick.localsportsclub.view.SyncerViewModel
import seepick.localsportsclub.view.VersionNotifier
import seepick.localsportsclub.view.activity.ActivityViewModel
import seepick.localsportsclub.view.common.showErrorDialog
import seepick.localsportsclub.view.freetraining.FreetrainingViewModel
import seepick.localsportsclub.view.notes.NotesViewModel
import seepick.localsportsclub.view.preferences.PreferencesViewModel
import seepick.localsportsclub.view.usage.UsageStorage
import seepick.localsportsclub.view.venue.VenueViewModel
import java.awt.event.ComponentEvent
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
            useFileAppender = config.logFileEnabled, packageSettings = mapOf(
                "seepick.localsportsclub" to Level.TRACE,
                "liquibase" to Level.INFO,
                "Exposed" to Level.INFO,
            )
        )
        val log = logger {}
        log.info { "Starting up application for environment: ${Environment.current.name}" }
        try {
            application {
                KoinApplication(application = {
                    modules(allModules(config))
                }) {
                    val keyboard: GlobalKeyboard = koinInject()
                    val applicationLifecycle: ApplicationLifecycle = koinInject()
                    applicationLifecycle.attachMacosQuitHandler() // when CMD+Q is executed (or from menubar)

                    val singlesService: SinglesService = koinInject()
                    val mainWindowState: MainWindowState = koinInject()
                    val windowPref = singlesService.windowPref ?: WindowPref.default
                    mainWindowState.update(windowPref.width, windowPref.height)
                    Window(
                        title = "LocalSportsClub v${AppPropertiesProvider.provide().version} ${if (Environment.current == Environment.Development) " - DEV 🤓" else ""}",
                        state = rememberWindowState(
                            width = windowPref.width.dp, height = windowPref.height.dp,
                            position = WindowPosition(windowPref.posX.dp, windowPref.posY.dp),
                        ),
                        onKeyEvent = { keyboard.process(it); false },
                        onCloseRequest = {
                            applicationLifecycle.onExit() // when the window close button is clicked
                            exitApplication()
                        },
                    ) {
                        window.addComponentListener(object : java.awt.event.ComponentAdapter() {
                            override fun componentResized(e: ComponentEvent) {
                                mainWindowState.update(window.width, window.height)
                            }
                        })

                        val mainViewModel = koinViewModel<MainViewModel>()
                        val desktop = java.awt.Desktop.getDesktop()
                        if (desktop.isSupported(java.awt.Desktop.Action.APP_PREFERENCES)) {
                            log.debug { "Registering preferences handler" }
                            desktop.setPreferencesHandler {
                                mainViewModel.changeScreen(Screen.Preferefences)
                            }
                        }

                        val syncer = koinInject<Syncer>()
                        val dataStorage = koinInject<DataStorage>()
                        val usageStorage = koinInject<UsageStorage>()
                        val bookingService = koinInject<BookingService>()
                        val syncerListeners = listOf(dataStorage, usageStorage, koinInject<SyncReporter>())
                        syncerListeners.forEach {
                            syncer.registerListener(it)
                            if (it.alsoRegisterForBooking()) {
                                bookingService.registerListener(it)
                            }
                        }

                        val syncProgress = koinInject<SyncProgress>()
                        syncProgress.register(koinViewModel<MainViewModel>())

                        val dataStorageListeners = listOf(
                            koinViewModel<SyncerViewModel>(),
                            koinViewModel<ActivityViewModel>(),
                            koinViewModel<FreetrainingViewModel>(),
                            koinViewModel<VenueViewModel>(),
                        )
                        dataStorageListeners.forEach {
                            dataStorage.registerListener(it)
                        }

                        val applicationLifecycleListeners = listOf(
                            usageStorage,
                            koinInject<VersionNotifier>(),
                            koinViewModel<MainViewModel>(),
                            koinViewModel<ActivityViewModel>(),
                            koinViewModel<FreetrainingViewModel>(),
                            koinViewModel<VenueViewModel>(),
                            koinViewModel<NotesViewModel>(),
                            koinViewModel<PreferencesViewModel>(),
                            object : ApplicationLifecycleListener {
                                override fun onExit() {
                                    singlesService.windowPref = WindowPref(
                                        width = window.width,
                                        height = window.height,
                                        posX = window.x,
                                        posY = window.y,
                                    )
                                }
                            },
                        )
                        applicationLifecycleListeners.forEach {
                            applicationLifecycle.registerListener(it)
                        }

                        window.addWindowListener(object : WindowAdapter() {
                            // they're working on proper onWindowReady here: https://youtrack.jetbrains.com/issue/CMP-5106
                            override fun windowOpened(e: WindowEvent?) {
                                applicationLifecycle.onStartUp()
                            }
                        })

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
        } catch (e: Exception) {
            log.error(e) { "Startup Error!" }
            showErrorDialog("Failed to start up the application. Shutting down.", e)
        }
    }
}
