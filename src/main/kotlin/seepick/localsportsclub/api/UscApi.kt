package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenueRequest

interface UscApi {
    suspend fun fetchVenues(): List<VenueInfo>
}

class MockUscApi : UscApi {
    private val log = logger {}
    override suspend fun fetchVenues(): List<VenueInfo> {
        log.debug { "Mock returning empty venues list." }
        return emptyList()
    }
}

class UscApiAdapter(
    private val venueApi: VenueApi,
    private val uscConfig: UscConfig,
) : UscApi {
    override suspend fun fetchVenues(): List<VenueInfo> =
        venueApi.fetchPages(VenueRequest(uscConfig.city, uscConfig.plan)).flatMap {
            VenueParser.parseHtmlContent(it.content)
        }
}
