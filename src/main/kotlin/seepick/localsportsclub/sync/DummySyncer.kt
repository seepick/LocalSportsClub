package seepick.localsportsclub.sync

import kotlinx.coroutines.delay
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueIdLink
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.DummyGenerator
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Plan
import java.time.LocalDate

class DummySyncer(
    private val venueRepo: VenueRepo,
    private val venueLinksRepo: VenueLinksRepo,
    private val activityRepo: ActivityRepo,
    private val freetrainingRepo: FreetrainingRepo,
    private val dispatcher: SyncerListenerDispatcher,
    private val clock: Clock,
    private val progress: SyncProgress,
) : Syncer {

    private var activityId = 1
    private var freetrainingId = 1

    override suspend fun sync() {
        progress.start()
        var isError = true
        try {
            sync2()
            isError = false
        } finally {
            progress.stop(isError)
        }
    }

    private suspend fun sync2() {
//        if (true) {
//            throw Exception("foobar")
//        }
//        if (true) {
//            val venue = generateVenues().first()//.copy(slug = "foobar")
//            val inserted = venueRepo.insert(venue)
//            dispatcher.dispatchOnVenueDbosAdded(listOf(inserted))
//            return
//        }
//        val bytes = withContext(Dispatchers.IO) {
//            DelayedSyncer::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
//        }
        generateVenues().forEach { venueDbo ->
            delay(200)
            val inserted = venueRepo.insert(venueDbo)
//            imageStorage.saveVenueImage("${inserted.slug}.png", bytes)
            dispatcher.dispatchOnVenueDbosAdded(listOf(inserted))
        }
        venueLinksRepo.insert(VenueIdLink(5, 6))

        generateActivities().forEach { activityDbo ->
            delay(50)
            activityRepo.insert(activityDbo)
            dispatcher.dispatchOnActivityDbosAdded(listOf(activityDbo))
        }

        var firstFreetraining: FreetrainingDbo? = null
        generateFreetrainings().forEach { freetrainingDbo ->
            if (firstFreetraining == null) firstFreetraining = freetrainingDbo
            delay(100)
            freetrainingRepo.insert(freetrainingDbo)
            dispatcher.dispatchOnFreetrainingDbosAdded(listOf(freetrainingDbo))
        }
        delay(300)
        dispatcher.dispatchOnFreetrainingDbosDeleted(listOf(firstFreetraining!!))
    }

    override fun registerListener(listener: SyncerListener) {
        dispatcher.registerListener(listener)
    }

    private fun generateVenues(): List<VenueDbo> = buildList {
        this += DummyGenerator.venue().copy(
            id = 1,
            name = "Double Shift",
            slug = "double-shift",
            facilities = "Gym,Sauna",
            rating = 3,
            description = (1..20).joinToString(" ") { "This is a very long description you cannot imagine $it." },
            importantInfo = (1..20).joinToString(" ") { "This is a very long info you cannot imagine $it." },
            openingTimes = (1..20).joinToString(" ") { "This is a very long opening time you cannot imagine $it." },
        )
        this += DummyGenerator.venue().copy(id = 2, name = "EMS", slug = "ems")
        this += DummyGenerator.venue().copy(id = 3, name = "Yoga Studio", slug = "yoga-studio")
        this += DummyGenerator.venue().copy(
            id = 4,
            name = "All Of It",
            slug = "aoi",
            rating = 5, facilities = "",
            officialWebsite = "https://www.allofit.com",
            description = "Some description", importantInfo = "important info", openingTimes = "opening times",
            isFavorited = true, isWishlisted = true,
            postalCode = "1000AB", street = "Main Street", addressLocality = "Amsterdam, Netherlands",
        )
        this += DummyGenerator.venue().copy(id = 5, name = "Zzz", slug = "zzz")
        this += DummyGenerator.venue().copy(id = 6, name = "Zzz2", slug = "zzz2")
        this += DummyGenerator.venue().copy(id = 7, name = "lowercased", slug = "lower-cased")
        this += DummyGenerator.venue().copy(id = 8, name = "Very long name it is very long indeed", slug = "very-long")
        this += DummyGenerator.venue().copy(id = 9, name = "Deleted", slug = "deleted", isDeleted = true)
        this += DummyGenerator.venue().copy(id = 10, name = "Hidden", slug = "hidden", isHidden = true)
    }

    private fun generateActivities(): List<ActivityDbo> = buildList {
        val now = clock.now().withMinute(0).withSecond(0)
        this += dummyActivity(activityId++, 1).copy(name = "Gym workout", category = "OokGym")
        this += dummyActivity(activityId++, 2).copy(
            name = "EMS workout", category = "EMS",
            from = now.plusDays(3), to = now.plusDays(3).plusMinutes(20)
        )
        this += dummyActivity(activityId++, 4).copy(
            name = "You will be there", state = ActivityState.Booked, teacher = "Brittany Mock",
            from = now.plusHours(3), to = now.plusHours(4),
        )
        this += dummyActivity(activityId++, 4).copy(
            name = "You were there", state = ActivityState.Checkedin,
            from = now.minusDays(1), to = now.minusDays(1).plusHours(1)
        )
        this += dummyActivity(activityId++, 4).copy(
            name = "Irrevelant Past (synced away)",
            from = now.minusDays(5), to = now.minusDays(5).plusHours(1)
        )
        this += dummyActivity(activityId++, 10).copy(
            name = "Hidden Activity",
            from = now.plusDays(2), to = now.plusDays(2).plusHours(1)
        )
//        repeat(30) {
//            this += dummyActivity(activityId++, 4).copy(name = "Many ${it + 1}")
//        }
    }

    private fun generateFreetrainings(): List<FreetrainingDbo> = buildList {
        this += dummyFreetraining().copy(
            venueId = 4,
            name = "Delete Me",
            category = "Nope",
            date = LocalDate.now(),
        )
        this += dummyFreetraining().copy(
            venueId = 4,
            name = "Free Sport",
            category = "Wellness",
            date = LocalDate.now().plusDays(1),
            state = FreetrainingState.Checkedin,
        )
        this += dummyFreetraining().copy(
            venueId = 4,
            name = "Irrevelant passed",
            category = "Wellness",
            date = LocalDate.now().minusDays(1),
        )
        this += dummyFreetraining().copy(
            venueId = 4,
            name = "Was Yesterday",
            date = LocalDate.now().minusDays(1),
        )
        this += dummyFreetraining().copy(
            venueId = 10,
            name = "Hidden Freetraining",
        )
    }


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
            description = null,
            state = ActivityState.Blank,
            cancellationLimit = null,
            planId = Plan.UscPlan.Small.id,
        )
    }

    private fun dummyFreetraining() = FreetrainingDbo(
        id = freetrainingId++,
        venueId = 1,
        name = "Name",
        category = "",
        date = LocalDate.now().plusDays(3),
        state = FreetrainingState.Blank,
        planId = Plan.UscPlan.Medium.id,
    )
}
