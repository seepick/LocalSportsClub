package seepick.localsportsclub

import io.kotest.core.spec.style.StringSpec
import org.koin.core.context.startKoin
import seepick.localsportsclub.AppConfig.ApiMode
import seepick.localsportsclub.AppConfig.DatabaseMode
import seepick.localsportsclub.AppConfig.SyncMode
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.sync.RealSyncerAdapter

class AppStartupKoinTest : StringSpec() {
    init {
        "When start koin Then be okay" {
            startKoin {
                modules(
                    allModules(
                        AppConfig(
                            database = DatabaseMode.InMemory,
                            api = ApiMode.Mock,
                            sync = SyncMode.Noop,
                        )
                    )
                )
            }.koin.also { koin ->
                koin.get<RealSyncerAdapter>()
                koin.get<VenuesRepo>()
            }
        }
    }
}
