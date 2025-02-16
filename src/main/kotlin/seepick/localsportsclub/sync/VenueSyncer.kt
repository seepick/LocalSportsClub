package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueIdLink
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.resolveVenueImage
import seepick.localsportsclub.service.workParallel
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

fun SyncProgress.onProgressVenues(detail: String?) {
    onProgress("Venues", detail)
}

class VenueSyncer(
    private val api: UscApi,
    private val venueRepo: VenueRepo,
    private val venueSyncInserter: VenueSyncInserter,
    private val dispatcher: SyncerListenerDispatcher,
    private val progress: SyncProgress,
) {
    private val log = logger {}

    suspend fun sync(session: PhpSessionId, plan: Plan, city: City) {
        log.info { "Syncing venues ..." }
        progress.onProgressVenues(null)
        val remoteVenuesBySlug = api.fetchVenues(session, VenuesFilter(city, plan)).associateBy { it.slug }
        log.debug { "Received ${remoteVenuesBySlug.size} remote venues." }
        val localVenues = venueRepo.selectAllByCity(city.id)
        val markDeleted = localVenues.filter { !it.isDeleted }.associateBy { it.slug }.minus(remoteVenuesBySlug.keys)
        // this also means that the "hidden linked ones" will be deleted
        log.debug { "Going to mark ${markDeleted.size} venues as deleted." }
        dispatcher.dispatchOnVenueDbosMarkedDeleted(markDeleted.values.toList())
        markDeleted.values.forEach {
            venueRepo.update(it.copy(isDeleted = true))
        }

        val missingVenues = remoteVenuesBySlug.minus(localVenues.associateBy { it.slug }.keys)
        venueSyncInserter.fetchInsertAndDispatch(session, city, missingVenues.keys.toList())
    }
}

class VenueSlugLink(
    val slug1: String, val slug2: String
) {
    override fun toString() = "VenueSlugLink[$slug1/$slug2]"
    override fun hashCode() = slug1.hashCode() + slug2.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is VenueSlugLink) return false
        return (slug1 == other.slug1 && slug2 == other.slug2) ||
                (slug1 == other.slug2 && slug2 == other.slug1)
    }
}

interface VenueSyncInserter {
    suspend fun fetchInsertAndDispatch(
        session: PhpSessionId,
        city: City,
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
    private val progress: SyncProgress,
) : VenueSyncInserter {
    private val log = logger {}

    private var venueCount = AtomicInteger(-1)
    private fun SyncProgress.onProgressVenueItem() {
        val current = venueCount.getAndDecrement().let { if (it < 0) 0 else it }
        if (current % 25 == 0) {
            onProgressVenues("Load $current")
        }
    }

    override suspend fun fetchInsertAndDispatch(
        session: PhpSessionId,
        city: City,
        venueSlugs: List<String>,
        prefilledNotes: String,
    ) {
        log.debug { "Fetching details, image, linking and dispatching for ${venueSlugs.size} venues." }
        venueCount.set(venueSlugs.size)

        val newDbos = mutableListOf<VenueDbo>()
        val newLinks = mutableSetOf<VenueSlugLink>()
        progress.onProgressVenues("Load ${venueSlugs.size}")
        fetchAllInsertDispatch(
            session,
            city,
            venueSlugs,
            prefilledNotes,
            newDbos = newDbos,
            newLinks = newLinks,
        )
        log.trace { "Inserting links: $newLinks" }
        val existingVenues = venueRepo.selectAllByCity(city.id).associate { it.slug to it.id }
        newLinks.map { newLink ->
            VenueIdLink(
                existingVenues[newLink.slug1] ?: error("Venue1 not found by slug for link: $newLink"),
                existingVenues[newLink.slug2] ?: error("Venue2 not found by slug for link: $newLink"),
            )
        }.toSet().distinct().forEach(venueLinksRepo::insert)

        dispatcher.dispatchOnVenueDbosAdded(newDbos)
    }

    private suspend fun fetchAllInsertDispatch(
        session: PhpSessionId,
        city: City,
        venueSlugs: List<String>,
        prefilledNotes: String = "",
        newDbos: MutableList<VenueDbo>,
        newLinks: MutableSet<VenueSlugLink>,
    ) {
        log.trace { "fetchAllInsertDispatch(venueSlugs=$venueSlugs, newLinks=$newLinks)" }
        newDbos += workParallel(min(venueSlugs.size, 40), venueSlugs) { slug ->
            fetchDetailsDownloadImage(session, city, slug, newLinks).copy(notes = prefilledNotes)
        }.map { dbo ->
            venueRepo.insert(dbo)
        }
        linkVenues(session, city, newDbos, newLinks)
    }

    private suspend fun linkVenues(
        session: PhpSessionId,
        city: City,
        newDbos: MutableList<VenueDbo>,
        newLinks: MutableSet<VenueSlugLink>,
    ) {
        val existingVenues = venueRepo.selectAllByCity(city.id).associate { it.slug to it.id }
        val newLinkSlugs = newLinks.map { it.slug1 }.plus(newLinks.map { it.slug2 }).distinct()
        val missingVenuesBySlug = newLinkSlugs - existingVenues.keys
        log.trace { "linkVenues ... missingVenuesBySlug=$missingVenuesBySlug" }
        if (missingVenuesBySlug.isNotEmpty()) {
            log.debug { "Fetching additional ${missingVenuesBySlug.size} missing venues (by linking)." }
            venueCount.addAndGet(missingVenuesBySlug.size)
            fetchAllInsertDispatch(
                session,
                city,
                missingVenuesBySlug,
                prefilledNotes = "[SYNC] refetch due to missing venue link",
                newDbos,
                newLinks,
            )
        }
    }

    private suspend fun fetchDetailsDownloadImage(
        session: PhpSessionId,
        city: City,
        slug: String,
        venueSlugLinks: MutableSet<VenueSlugLink>
    ): VenueDbo {
        progress.onProgressVenueItem()
        val details = api.fetchVenueDetail(session, slug)
        details.linkedVenueSlugs.forEach {
            venueSlugLinks += VenueSlugLink(details.slug, it)
        }
        return details.toDbo(city.id).ensureHasImageIfPresent(details)
    }

    private suspend fun VenueDbo.ensureHasImageIfPresent(details: VenueDetails): VenueDbo {
        return if (details.originalImageUrl == null) this else {
            val fileName = "${details.slug}.png"
            if (FileResolver.resolveVenueImage(fileName).exists()) {
                log.trace { "Venue image [$fileName] already exists, skip downloading it." }
            } else {
                downloadAndSaveImage(fileName, details.originalImageUrl)
            }
            this.copy(imageFileName = fileName)
        }
    }

    private suspend fun downloadAndSaveImage(fileName: String, originalImageUrl: Url) {
        val downloadedBytes = downloader.downloadVenueImage(originalImageUrl)
        imageStorage.saveAndResizeVenueImage(fileName, downloadedBytes)
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
