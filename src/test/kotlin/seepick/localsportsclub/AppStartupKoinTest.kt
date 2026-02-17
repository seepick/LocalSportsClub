package seepick.localsportsclub

import io.kotest.core.spec.style.StringSpec
import org.koin.core.context.startKoin
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.sync.Downloader
import seepick.localsportsclub.sync.Syncer

class AppStartupKoinTest : StringSpec() {
    init {
        "When start koin Then be okay" {
            startKoin {
                modules(
                    allModules(
                        LscConfig(
                            database = DatabaseMode.InMemory,
                            apiMode = ApiMode.Mock,
                            sync = SyncMode.Noop,
                            gcal = GcalMode.Noop,
                        )
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
