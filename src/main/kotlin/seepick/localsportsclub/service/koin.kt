package seepick.localsportsclub.service

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.AppConfig
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.service.singles.SinglesServiceImpl

fun serviceModule(config: AppConfig) = module {
    single { SystemClock } bind Clock::class
    single { httpClient } bind HttpClient::class
    singleOf(::DataStorage)
    singleOf(::SinglesServiceImpl) bind SinglesService::class
    singleOf(::BookingService)
    single { if (config.versionCheckEnabled) OnlineVersionChecker(get()) else NoopVersionChecker } bind VersionChecker::class
    single {
        FileSystemImageStorage(
            venueImagesFolder = FileResolver.resolve(DirectoryEntry.VenueImages),
        )
    } bind (ImageStorage::class)
}
