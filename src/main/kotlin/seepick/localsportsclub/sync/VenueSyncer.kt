package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.service.toVenue
import seepick.localsportsclub.service.workParallel

class VenueSyncer(
    private val api: UscApi,
    private val venuesRepo: VenuesRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val city: City,
    private val plan: PlanType,
    private val syncDispatcher: SyncDispatcher,
    private val baseUrl: String,
    private val imageFetcher: ImageFetcher,
) {
    private val log = logger {}

    suspend fun sync() {
        log.info { "Syncing venues ..." }
        val remoteVenues = api.fetchVenues(VenuesFilter(city, plan))
        val localVenues = venuesRepo.selectAll()

        val remoteSlugs = remoteVenues.associateBy { it.slug }
        val localSlugs = localVenues.associateBy { it.slug }

        val markDeleted = localSlugs.minus(remoteSlugs.keys) // FIXME mark them as deleted

        val missingVenues = remoteSlugs.minus(localSlugs.keys)
        log.debug { "Fetching details for ${missingVenues.size} venues." }

        transaction {
            val newVenueLinksBySlugs = mutableListOf<Pair<String, String>>()
            workParallel(10, missingVenues.values.toList()) { venue ->
                val details = api.fetchVenueDetail(venue.slug)
                val dbo = venue.toDbo().copy(officialWebsite = details.websiteUrl)
                details.linkedVenueSlugs.forEach {
                    newVenueLinksBySlugs += venue.slug to it
                }
                val inserted = venuesRepo.insert(dbo)
                imageFetcher.saveVenueImage(inserted.id, Url(venue.imageUrl))
                syncDispatcher.dispatchVenueAdded(dbo.toVenue(baseUrl))
            }
            val venuesIdBySlug = venuesRepo.selectAll().associate { it.slug to it.id }
            newVenueLinksBySlugs.map { venuesIdBySlug[it.first]!! to venuesIdBySlug[it.second]!! }.forEach {
                venueLinksRepo.insert(it.first, it.second)
            }
        }
    }

    private fun VenueInfo.toDbo() = VenueDbo(
        id = -1,
        name = title,
        slug = slug,
        cityId = city.id,
        // get more from details request
        facilities = "",
        officialWebsite = null,
        rating = 0,
        notes = "",
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
    )
}
