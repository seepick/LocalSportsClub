package seepick.localsportsclub.sync

import kotlinx.coroutines.delay
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.date.Clock
import java.time.LocalDate

class DummySyncer(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
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
        venueLinksRepo.insert(5, 6)

        generateActivities().forEach { activityDbo ->
            delay(300)
            activityRepo.insert(activityDbo)
            dispatcher.dispatchOnActivityDboAdded(activityDbo)
        }

        generateFreetrainings().forEach { freetrainingDbo ->
            delay(300)
            freetrainingRepo.insert(freetrainingDbo)
            dispatcher.dispatchOnFreetrainingDboAdded(freetrainingDbo)
        }
    }

    private fun generateVenues(): List<VenueDbo> =
        listOf(
            dummyVenue().copy(
                id = 1,
                name = "Double Shift",
                slug = "double-shift",
                facilities = "Gym,Sauna",
                rating = 3,
                description = "Some\nline"
            ),
            dummyVenue().copy(id = 2, name = "EMS", slug = "ems"),
            dummyVenue().copy(id = 3, name = "Yoga Studio", slug = "yoga-studio"),
            dummyVenue().copy(
                id = 4,
                name = "All Of It", slug = "aoi",
                rating = 5, facilities = "",
                officialWebsite = "https://www.allofit.com",
                description = "Some description", importantInfo = "important info", openingTimes = "opening times",
                isFavorited = true, isWishlisted = true,
                postalCode = "1000AB", street = "Main Street", addressLocality = "Amsterdam, Netherlands",
            ),
            dummyVenue().copy(id = 5, name = "Zzz", slug = "zzz"),
            dummyVenue().copy(id = 6, name = "Zzz2", slug = "zzz2"),
            dummyVenue().copy(id = 7, name = "lowercased", slug = "lower-cased"),
            dummyVenue().copy(id = 8, name = "Very long name it is very long indeed", slug = "very-long"),
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
            ),
        )
    }


    private fun generateFreetrainings(): List<FreetrainingDbo> =
        listOf(
            FreetrainingDbo(
                id = 1,
                venueId = 4,
                name = "Free Sport",
                category = "Wellness",
                date = LocalDate.now(),
                wasCheckedin = false,
            ),
            FreetrainingDbo(
                id = 2,
                venueId = 4,
                name = "Free Sport",
                category = "Wellness",
                date = LocalDate.now().minusDays(1),
                wasCheckedin = true,
            )
        )

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
            teacher = null,
            isBooked = false,
            wasCheckedin = false,
        )
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }
}
