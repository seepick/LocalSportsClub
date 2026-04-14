package seepick.localsportsclub

import com.github.seepick.uscclient.model.UscLang
import io.kotest.core.spec.style.StringSpec
import lsc.repo.VenueRepo
import lsc.repo.exposedPersistenceModule
import org.koin.core.context.startKoin
import seepick.localsportsclub.devApp.development
import seepick.localsportsclub.gcal.gcalModule
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.VenueService
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Downloader
import seepick.localsportsclub.sync.Syncer
import seepick.localsportsclub.sync.syncModule
import seepick.localsportsclub.view.VersionNotifier
import seepick.localsportsclub.view.activity.ActivityViewModel

class AppStartupKoinTest : StringSpec() {
    init {
        "When start koin Then be okay" {
            startKoin {
                val config = LscConfig(
                    syncDaysAhead = 1,
                    apiLang = UscLang.English,
                    logbackFileEnabled = false,
                    versionCheckEnabled = false,
                    appDirectory = LscConfig.development.appDirectory,
                )
                modules(
                    allModules(
                        config,
                        persistenceModule = exposedPersistenceModule(config.fileResolver.resolve(DirectoryEntry.Database)),
                        gcalModule = gcalModule(),
                        uscClientModule = uscClientModule(config),
                        syncModule = syncModule(config),
                    )
                )
            }.koin.also { koin ->
                koin.get<Syncer>()
                koin.get<VenueRepo>()
                koin.get<DataStorage>()
                koin.get<Downloader>()
                koin.get<VenueService>()
                koin.get<VersionNotifier>()
                koin.get<ActivityViewModel>()
            }
        }
    }
}
