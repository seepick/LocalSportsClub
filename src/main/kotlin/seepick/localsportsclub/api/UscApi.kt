package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenuesFilter

interface UscApi {
    suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo>
}

class MockUscApi : UscApi {
    private val log = logger {}
    override suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo> {
        log.debug { "Mock returning empty venues list." }
        return emptyList()
    }
}

class UscApiAdapter(
    private val venueApi: VenueApi,
) : UscApi {
    override suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo> =
        venueApi.fetchPages(filter).flatMap {
            VenueParser.parseHtmlContent(it.content)
        }
}
