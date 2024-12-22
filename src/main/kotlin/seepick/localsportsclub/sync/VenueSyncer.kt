package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import net.coobird.thumbnailator.Thumbnails
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenuesRepo
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.model.toVenue
import seepick.localsportsclub.service.workParallel
import java.io.ByteArrayOutputStream

class VenueSyncer(
    private val api: UscApi,
    private val venuesRepo: VenuesRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val city: City,
    private val plan: PlanType,
    private val syncDispatcher: SyncDispatcher,
    private val baseUrl: String,
    private val downloader: Downloader,
    private val imageStorage: ImageStorage,
) {
    private val log = logger {}

    suspend fun sync() {
        log.info { "Syncing venues ..." }
        val remoteSlugs = api.fetchVenues(VenuesFilter(city, plan)).associateBy { it.slug }
        val localSlugs = venuesRepo.selectAll().associateBy { it.slug }

        val markDeleted = localSlugs.minus(remoteSlugs.keys)
        markDeleted.values.forEach {
            venuesRepo.update(it.copy(isDeleted = true))
        }

        val missingVenues = remoteSlugs.minus(localSlugs.keys)
        log.debug { "Fetching details for ${missingVenues.size} venues." }
        val newVenueLinksBySlugs = mutableListOf<Pair<String, String>>()
        workParallel(5, missingVenues.values.toList()) { venue ->
            fetchDetails(venue.slug, newVenueLinksBySlugs)
        }

        linkVenues(newVenueLinksBySlugs)
    }

    private suspend fun linkVenues(newLinks: MutableList<Pair<String, String>>) {
        val existingVenues = venuesRepo.selectAll().associate { it.slug to it.id }

        val missingVenuesBySlug = (newLinks.mapNotNull {
            if (existingVenues.containsKey(it.first)) null else it.first
        } + newLinks.mapNotNull {
            if (existingVenues.containsKey(it.second)) null else it.second
        }).distinct().sorted()

        if (missingVenuesBySlug.isEmpty()) {
            newLinks.map {
                existingVenues[it.first]!! to existingVenues[it.second]!!
            }.forEach {
                venueLinksRepo.insert(it.first, it.second)
            }
        } else {
            log.debug { "Fetching additional ${missingVenuesBySlug.size} missing venues (by linking)." }
            workParallel(5, missingVenuesBySlug) { slug ->
                fetchDetails(slug, newLinks)
            }
            linkVenues(newLinks)
        }
    }

    private suspend fun fetchDetails(slug: String, venueLinks: MutableList<Pair<String, String>>) {
        val details = api.fetchVenueDetail(slug)
        details.linkedVenueSlugs.forEach {
            venueLinks += details.slug to it
        }
        val inserted = venuesRepo.insert(
            details.toDbo()
        ).let {
            if (details.originalImageUrl == null) it else enhanceImage(it, details.originalImageUrl)
        }

        syncDispatcher.dispatchVenueAdded(inserted.toVenue(baseUrl))
    }

    // can do that only AFTER was persisted, as we need the internal ID for the filename
    private suspend fun enhanceImage(venue: VenueDbo, originalImageUrl: Url): VenueDbo {
        val downloadedBytes = downloader.downloadVenueImage(originalImageUrl)

        val output = ByteArrayOutputStream()
        Thumbnails.of(downloadedBytes.inputStream())
            .size(400, 400)
            .keepAspectRatio(true)
            .outputFormat("png")
            .outputQuality(1.0)
            .toOutputStream(output)
        val resizedBytes = output.toByteArray()

        val fileName = "${venue.id}.png"
        imageStorage.saveVenue(fileName, resizedBytes)
        return venuesRepo.update(venue.copy(imageFileName = fileName))
    }

    private fun VenueDetails.toDbo() = VenueDbo(
        id = -1,
        name = title,
        slug = slug,
        cityId = city.id,
        imageFileName = null, // not yet set
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
}
