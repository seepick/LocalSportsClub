package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenuesRepo
import java.util.concurrent.ConcurrentLinkedQueue

private data class InsertVenue(
    val dbo: VenueDbo,
    val linkedVenueSlugs: List<String>,
)

private val log = logger {}
fun <T> workParallel(coroutineCount: Int, data: List<T>, processor: suspend (T) -> Unit) {
    val items = ConcurrentLinkedQueue(data.toMutableList())
    runBlocking {
        (1..coroutineCount).map { coroutine ->
            log.debug { "Starting coroutine $coroutine/$coroutineCount ..." }
            launch {
                var item = items.poll()
                while (item != null) {
                    processor(item)
                    item = items.poll()
                }
            }
        }.joinAll()
    }
}

class VenueSyncer(
    private val api: UscApi,
    private val venuesRepo: VenuesRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val city: City,
    private val plan: PlanType,
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
                // FIXME also download pictures
                val dbo = venue.toDbo().copy(officialWebsite = details.websiteUrl)
                details.linkedVenueSlugs.forEach {
                    newVenueLinksBySlugs += venue.slug to it
                }
                venuesRepo.insert(dbo)
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
        note = "",
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
    )
}
