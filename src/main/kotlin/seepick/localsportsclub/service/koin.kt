package seepick.localsportsclub.service

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.DataStorage

fun serviceModule() = module {
    single { SystemClock } bind Clock::class
    single { httpClient } bind HttpClient::class
    singleOf(::DataStorage)
    singleOf(::SinglesService)
    single {
        FileSystemImageStorage(
            venueImagesFolder = FileResolver.resolve(DirectoryEntry.VenueImages),
        )
    } bind (ImageStorage::class)
}
