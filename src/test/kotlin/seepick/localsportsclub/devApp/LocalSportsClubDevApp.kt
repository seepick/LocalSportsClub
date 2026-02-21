package seepick.localsportsclub.devApp

import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.gcal.noopGcalModule
import seepick.localsportsclub.startApplication
import java.io.File

object LocalSportsClubDevApp {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = LscConfig.development
        startApplication(
            config = config,
//            persistenceModule = inmemoryPersistenceModule(),
            gcalModule = noopGcalModule(),
//            uscClientModule = mockUscClientModule(),
//            syncModule = devSyncModule(SyncMode.Dummy, config),
        )
    }
}

private const val APP_DIRECTORY = ".lsc-dev"

private val devConfig = LscConfig(
    windowTitleSuffix = " - DEV 🤓",
    appDirectory = File(File(System.getProperty("user.home")), APP_DIRECTORY),
    logbackFileEnabled = false,
    versionCheckEnabled = false,
)

val LscConfig.Companion.development get() = devConfig
