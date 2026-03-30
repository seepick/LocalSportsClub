package seepick.localsportsclub.devApp

import com.github.seepick.uscclient.login.Credentials
import seepick.localsportsclub.LscConfig
import seepick.localsportsclub.startApplication
import java.io.File

fun Credentials.Companion.dummy() = Credentials(
    username = "dummyUser",
    password = "dummyPass",
)

object LocalSportsClubDevApp {

    @JvmStatic
    fun main(args: Array<String>) {
//        overrideTheme(isDark = false)

        val config = LscConfig.development
        startApplication(
            config = config,
            onStartup = { koin ->
//                val singlesService = koin.get<SinglesService>()
//                val creds = Credentials.dummy()
//                singlesService.verifiedUscCredentials = creds // to enable sync
//                singlesService.preferences = singlesService.preferences.copy(
//                    city = City.Amsterdam, // to enable sync
//                    uscCredentials = creds, // view it in UI
//                )
            },
//            gcalModule = noopGcalModule(),
//            uscClientModule = mockUscClientModule(),
//            syncModule = devSyncModule(SyncMode.Dummy, config),
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
)

val LscConfig.Companion.development get() = devConfig
