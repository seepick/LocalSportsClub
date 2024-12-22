package seepick.localsportsclub.api

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.api.venue.VenueApi
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenueParser
import seepick.localsportsclub.api.venue.VenuesFilter

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
        VenueDetails(
            title = "My Title",
            slug = "my-title",
            description = "mock description",
            linkedVenueSlugs = emptyList(),
            websiteUrl = null,
            disciplines = listOf("Yoga"),
            importantInfo = "impo info",
            openingTimes = "open altijd",
            longitude = "1.0",
            latitude = "2.0",
            originalImageUrl = Url("http://mock/test.png"),
            postalCode = "1001AA",
            streetAddress = "Main Street 42",
            addressLocality = "Amsterdam, Netherlands"
        )
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
