package seepick.localsportsclub.devApp

import seepick.localsportsclub.ApiMode
import seepick.localsportsclub.GcalMode
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.startApplication
import seepick.localsportsclub.sync.SyncMode
import java.io.File

object LocalSportsClubDevApp {
    @JvmStatic
    fun main(args: Array<String>) {
        startApplication(
            config = LscConfig.development,
//            persistenceModule = inmemoryPersistenceModule(),
        )
    }
}

private const val APP_DIRECTORY = ".lsc-dev"

private val devConfig = LscConfig(
    windowTitleSuffix = " - DEV 🤓",
    appDirectory = File(File(System.getProperty("user.home")), APP_DIRECTORY),
    logbackFileEnabled = false,
    versionCheckEnabled = false,

    apiMode = ApiMode.RealHttp,
//        apiMode = ApiMode.Mock,

    syncMode = SyncMode.Real,
//        syncMode = SyncMode.Dummy,
//        syncMode = SyncMode.Noop,
//        syncMode = SyncMode.Delayed,

//        gcalMode = GcalMode.Real,
    gcalMode = GcalMode.Noop,
)

val LscConfig.Companion.development get() = devConfig
