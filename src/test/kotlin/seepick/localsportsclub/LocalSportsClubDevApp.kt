package seepick.localsportsclub

import seepick.localsportsclub.sync.SyncMode
import java.io.File

object LocalSportsClubDevApp {
    @JvmStatic
    fun main(args: Array<String>) {
        startApplication(LscConfig.development)
    }
}

private const val APP_DIRECTORY = ".lsc-dev"

val LscConfig.Companion.development
    get() = LscConfig(
        windowTitleSuffix = " - DEV 🤓",
        appDirectory = File(File(System.getProperty("user.home")), APP_DIRECTORY),
        logbackFileEnabled = false,
        versionCheckEnabled = false,

        apiMode = ApiMode.Mock,
//        apiMode = ApiMode.RealHttp,
        syncMode = SyncMode.Real,
//        syncMode = SyncMode.Dummy,
//        syncMode = SyncMode.Noop,
//        syncMode = SyncMode.Delayed,
        gcalMode = GcalMode.Noop,
//        gcalMode = GcalMode.Real,
        databaseMode = DatabaseMode.Exposed,
//        databaseMode = DatabaseMode.InMemory,
    )
