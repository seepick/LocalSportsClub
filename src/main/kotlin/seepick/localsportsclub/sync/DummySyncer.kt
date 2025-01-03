package seepick.localsportsclub.sync

import kotlinx.coroutines.delay
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.Clock

class DummySyncer(
    private val venueRepo: VenueRepo,
    private val activityRepo: ActivityRepo,
//    private val imageStorage: ImageStorage,
    private val dispatcher: SyncerListenerDispatcher,
    private val clock: Clock,
) : Syncer {

    override suspend fun sync() {
//        val bytes = withContext(Dispatchers.IO) {
//            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
//        }
        generateVenues().forEach { venueDbo ->
            delay(300)
            val inserted = venueRepo.insert(venueDbo)
//            imageStorage.saveVenueImage("${inserted.slug}.png", bytes)
            dispatcher.dispatchOnVenueDboAdded(inserted)
        }
        generateActivities().forEach { activityDbo ->
            delay(300)
            activityRepo.insert(activityDbo)
            dispatcher.dispatchOnActivityDboAdded(activityDbo)
        }
    }

    private fun generateVenues(): List<VenueDbo> =
        listOf(
            dummyVenue().copy(
                name = "Double Shift",
                slug = "double-shift",
                facilities = "Gym,Sauna",
                rating = 3,
                description = "Some\nline"
            ),
            dummyVenue().copy(name = "EMS", slug = "ems"),
            dummyVenue().copy(name = "Yoga Studio", slug = "yoga-studio"),
            dummyVenue().copy(
                name = "All Of It", slug = "aoi",
                rating = 5, facilities = "",
                officialWebsite = "https://www.allofit.com",
                description = "Some description", importantInfo = "important info", openingTimes = "opening times",
                isFavorited = true, isWishlisted = true,
                postalCode = "1000AB", street = "Main Street", addressLocality = "Amsterdam, Netherlands",
            ),
            dummyVenue().copy(name = "Zzz", slug = "zzz"),
            dummyVenue().copy(name = "lowercased", slug = "lower-cased"),
            dummyVenue().copy(name = "Very long name it is very long indeed", slug = "very-long"),
        )

    private fun generateActivities(): List<ActivityDbo> {
        val now = clock.now().withMinute(0).withSecond(0)
        var activityId = 1
        return listOf(
            dummyActivity(activityId++, 1).copy(name = "Gym workout", category = "OokGym"),
            dummyActivity(activityId++, 2).copy(
                name = "EMS workout", category = "EMS",
                from = now.plusDays(3), to = now.plusDays(3).plusMinutes(20)
            ),
            dummyActivity(activityId++, 4).copy(
                name = "You will be there", isBooked = true,
                from = now.plusHours(1), to = now.plusHours(2)
            ),
            dummyActivity(activityId++, 4).copy(
                name = "You were there", wasCheckedin = true,
                from = now.minusDays(1), to = now.minusDays(1).plusHours(1)
            )
        )
    }

    private fun dummyVenue() = VenueDbo(
        id = 0,
        name = "",
        slug = "",
        facilities = "",
        cityId = City.Amsterdam.id,
        officialWebsite = null,
        rating = 0,
        notes = "",
        imageFileName = null,
        postalCode = "",
        street = "",
        addressLocality = "",
        latitude = "",
        longitude = "",
        description = "",
        importantInfo = null,
        openingTimes = null,
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
    )

    private fun dummyActivity(activityId: Int, venueId: Int): ActivityDbo {
        val now = clock.now().withMinute(0).withSecond(0)
        return ActivityDbo(
            id = activityId,
            venueId = venueId,
            name = "No Name",
            category = "",
            spotsLeft = 4,
            from = now.plusDays(1),
            to = now.plusDays(1).plusMinutes(75),
            isBooked = false,
            wasCheckedin = false,
        )
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }
}
