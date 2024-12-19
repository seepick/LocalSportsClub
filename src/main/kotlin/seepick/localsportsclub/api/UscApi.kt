package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.api.venueDetails.VenueDetails

interface UscApi {
    suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo>
    suspend fun fetchVenueDetail(slug: String): VenueDetails
}

class MockUscApi : UscApi {
    private val log = logger {}
    override suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo> {
        log.debug { "Mock returning empty venues list." }
        return emptyList()
    }

    override suspend fun fetchVenueDetail(slug: String): VenueDetails =
        VenueDetails(emptyList(), null)
}

class UscApiAdapter(
    private val venueApi: VenueApi,
) : UscApi {
    override suspend fun fetchVenues(filter: VenuesFilter): List<VenueInfo> =
        venueApi.fetchPages(filter).flatMap {
            VenueParser.parseHtmlContent(it.content)
        }

    override suspend fun fetchVenueDetail(slug: String): VenueDetails =
        venueApi.fetchDetails(slug)
}
