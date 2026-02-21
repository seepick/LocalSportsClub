package seepick.localsportsclub

import com.github.seepick.uscclient.model.UscLang
import io.kotest.core.spec.style.StringSpec
import org.koin.core.context.startKoin
import seepick.localsportsclub.devApp.development
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.persistence.inmemoryPersistenceModule
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Downloader
import seepick.localsportsclub.sync.SyncMode
import seepick.localsportsclub.sync.Syncer

class AppStartupKoinTest : StringSpec() {
    init {
        "When start koin Then be okay" {
            startKoin {
                modules(
                    allModules(
                        LscConfig(
                            apiMode = ApiMode.Mock,
                            syncMode = SyncMode.Noop,
                            gcalMode = GcalMode.Noop,
                            syncDaysAhead = 1,
                            apiLang = UscLang.English,
                            logbackFileEnabled = false,
                            versionCheckEnabled = false,
                            appDirectory = LscConfig.development.appDirectory,
                        ),
                        persistenceModule = inmemoryPersistenceModule(),
                    )
                )
            }.koin.also { koin ->
                koin.get<Syncer>()
                koin.get<VenueRepo>()
                koin.get<DataStorage>()
                koin.get<Downloader>()
            }
        }
    }
}
