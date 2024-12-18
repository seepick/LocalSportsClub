package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenuesRepo

interface Syncer {
    fun sync()
}

class RealSyncer(
    private val api: UscApi,
    private val venuesRepo: VenuesRepo,
    private val city: City,
    private val plan: PlanType,
) : Syncer {
    private val log = logger {}
    override fun sync() {
        log.debug { "Syncing ..." }
        runBlocking {
            syncVenues()
        }
    }

    private suspend fun syncVenues() {
        val remoteVenues = api.fetchVenues(VenuesFilter(city, plan))
        val localVenues = venuesRepo.selectAll()

        val remoteSlugs = remoteVenues.associateBy { it.slug }
        val localSlugs = localVenues.associateBy { it.slug }

        val toBeInserted = remoteSlugs.minus(localSlugs.keys)
        log.debug { "Sync inserting ${toBeInserted.size} venues." }
        venuesRepo.persist(toBeInserted.values.map { it.toDbo() })
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
        note = "",
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
    )
}

