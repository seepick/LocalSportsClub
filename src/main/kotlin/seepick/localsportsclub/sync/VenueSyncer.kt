package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.UscConfig
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.resizeImage
import seepick.localsportsclub.service.workParallel

class VenueSyncer(
    private val api: UscApi,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    uscConfig: UscConfig,
) {
    private val log = logger {}
    private val city: City = uscConfig.city
    private val plan: PlanType = uscConfig.plan

    suspend fun sync() {
        log.info { "Syncing venues ..." }
        val remoteVenuesBySlug = api.fetchVenues(VenuesFilter(city, plan)).associateBy { it.slug }
        val localVenuesBySlug = venueRepo.selectAll().filter { !it.isDeleted }.associateBy { it.slug }

        val markDeleted = localVenuesBySlug.minus(remoteVenuesBySlug.keys)
        // this also means that the "hidden linked ones" will be deleted
        log.debug { "Going to mark ${markDeleted.size} venues as deleted." }
        markDeleted.values.forEach {
            venueRepo.update(it.copy(isDeleted = true))
        }

        val missingVenues = remoteVenuesBySlug.minus(localVenuesBySlug.keys)
        venueSyncInserter.fetchAllInsertDispatch(missingVenues.keys.toList())
    }

}

class VenueLink(
    val slug1: String, val slug2: String
) {
    override fun toString() = "VenueLink[$slug1/$slug2]"
    override fun hashCode() = slug1.hashCode() + slug2.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is VenueLink) return false
        return (slug1 == other.slug1 && slug2 == other.slug2) ||
                (slug1 == other.slug2 && slug2 == other.slug1)
    }
}

interface VenueSyncInserter {
    suspend fun fetchAllInsertDispatch(
        venueSlugs: List<String>,
        prefilledNotes: String = "",
    )
}

class VenueSyncInserterImpl(
    private val api: UscApi,
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val downloader: Downloader,
    private val imageStorage: ImageStorage,
    private val dispatcher: SyncerListenerDispatcher,
    uscConfig: UscConfig,
) : VenueSyncInserter {
    private val log = logger {}
    private val city = uscConfig.city

    override suspend fun fetchAllInsertDispatch(
        venueSlugs: List<String>,
        prefilledNotes: String,
    ) {
        log.debug { "Fetching details, image, linking and dispatching for ${venueSlugs.size} venues." }
        fetchAllInsertDispatch(venueSlugs, prefilledNotes, newLinks = mutableSetOf())
    }

    private suspend fun fetchAllInsertDispatch(
        venueSlugs: List<String>,
        prefilledNotes: String = "",
        newLinks: MutableSet<VenueLink>,
    ) {
        val dbos = workParallel(5, venueSlugs) { slug ->
            fetchDetailsDownloadImage(slug, newLinks).copy(notes = prefilledNotes)
        }.map { dbo ->
            venueRepo.insert(dbo)
        }
        linkVenues(newLinks)
        dbos.forEach { dbo ->
            dispatcher.dispatchOnVenueDboAdded(dbo)
        }
    }

    private suspend fun linkVenues(newLinks: MutableSet<VenueLink>) {
        val existingVenues = venueRepo.selectAll().associate { it.slug to it.id }

        val missingVenuesBySlug = (newLinks.mapNotNull {
            if (existingVenues.containsKey(it.slug1)) null else it.slug1
        } + newLinks.mapNotNull {
            if (existingVenues.containsKey(it.slug2)) null else it.slug2
        }).distinct().sorted()

        if (missingVenuesBySlug.isEmpty()) {
            newLinks.map {
                existingVenues[it.slug1]!! to existingVenues[it.slug2]!!
            }.forEach {
                venueLinksRepo.insert(it.first, it.second)
            }
        } else {
            log.debug { "Fetching additional ${missingVenuesBySlug.size} missing venues (by linking)." }
            fetchAllInsertDispatch(
                missingVenuesBySlug,
                prefilledNotes = "[SYNC] refetch due to missing venue link",
                newLinks
            )
        }
    }

    private suspend fun fetchDetailsDownloadImage(
        slug: String,
        venueLinks: MutableSet<VenueLink>
    ): VenueDbo {
        val details = api.fetchVenueDetail(slug)
        details.linkedVenueSlugs.forEach {
            venueLinks += VenueLink(details.slug, it)
        }
        val dbo = details.toDbo(city.id)
        return if (details.originalImageUrl == null) dbo else {
            val fileName = "${details.slug}.png"
            downloadAndSaveImage(fileName, details.originalImageUrl)
            dbo.copy(imageFileName = fileName)
        }
    }

    private suspend fun downloadAndSaveImage(fileName: String, originalImageUrl: Url) {
        val downloadedBytes = downloader.downloadVenueImage(originalImageUrl)
        val resizedBytes = resizeImage(downloadedBytes, 400 to 400)
        imageStorage.saveVenueImage(fileName, resizedBytes)
    }
}

private fun VenueDetails.toDbo(cityId: Int) = VenueDbo(
    id = -1,
    name = title,
    slug = slug,
    cityId = cityId,
    imageFileName = null, // will be set later after image download
    postalCode = postalCode,
    street = streetAddress,
    addressLocality = addressLocality,
    latitude = latitude,
    longitude = longitude,
    facilities = disciplines.joinToString(","),
    officialWebsite = websiteUrl?.toString(),
    description = description,
    openingTimes = openingTimes,
    importantInfo = importantInfo,
    rating = 0,
    notes = "",
    isFavorited = false,
    isWishlisted = false,
    isHidden = false,
    isDeleted = false,
)
