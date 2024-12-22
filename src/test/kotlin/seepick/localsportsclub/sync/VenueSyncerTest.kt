package seepick.localsportsclub.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venue.VenuesFilter
import seepick.localsportsclub.api.venueDetails
import seepick.localsportsclub.api.venueInfo
import seepick.localsportsclub.imageUrl
import seepick.localsportsclub.persistence.InMemoryVenueLinksRepo
import seepick.localsportsclub.persistence.InMemoryVenueRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.testInfra.DbListener
import seepick.localsportsclub.persistence.testInfra.venueDbo
import seepick.localsportsclub.service.MemorizableImageStorage


class VenueSyncerTest : StringSpec() {

    private val remoteVenue = Arb.venueInfo().next()
    private val remoteDetails = Arb.venueDetails().next()
    private lateinit var api: UscApi
    private lateinit var venuesRepo: InMemoryVenueRepo
    private lateinit var venueLinksRepo: InMemoryVenueLinksRepo
    private lateinit var imageStorage: MemorizableImageStorage
    private lateinit var syncer: VenueSyncer
    private val city = City.entries.random()
    private val plan = PlanType.entries.random()
    private val syncVenueDbosAdded = mutableListOf<VenueDbo>()

    init {
        extension(DbListener())

        "Given api returns 1 and db has 0 When sync Then inserted, synced, and image saved" {
            val imageUrl = Arb.imageUrl().next()
            coEvery {
                api.fetchVenues(eq(VenuesFilter(city, plan)))
            } returns listOf(remoteVenue.copy(imageUrl = imageUrl))
            coEvery { api.fetchVenueDetail(eq(remoteVenue.slug)) } returns
                    remoteDetails.copy(linkedVenueSlugs = emptyList(), originalImageUrl = Arb.imageUrl().next())

            syncer.sync()

            venuesRepo.stored.should {
                val stored = it.values.shouldBeSingleton().first()
                // can't test on ID
                stored.name shouldBe remoteDetails.title
                stored.slug shouldBe remoteDetails.slug

                syncVenueDbosAdded.shouldBeSingleton().first().slug shouldBe remoteDetails.slug
                val expectedImageFileName = "${stored.id}.png"
                imageStorage.savedVenueImages.shouldBeSingleton().first().first shouldBe expectedImageFileName
            }
        }
        "Given api returns 0 and db has 1 When sync Then mark as deleted" {
            coEvery { api.fetchVenues(eq(VenuesFilter(city, plan))) } returns emptyList()
            venuesRepo.insert(Arb.venueDbo().next().copy(isDeleted = false))

            syncer.sync()

            venuesRepo.stored.values.shouldBeSingleton().first().isDeleted shouldBe true
        }
        "Given api returns 1 with linked and db has this 1 When sync Then link them" {
            val yetExisting = venuesRepo.insert(Arb.venueDbo().next())
            coEvery { api.fetchVenues(eq(VenuesFilter(city, plan))) } returns listOf(remoteVenue)
            coEvery { api.fetchVenueDetail(eq(remoteVenue.slug)) } returns remoteDetails
                .copy(linkedVenueSlugs = listOf(yetExisting.slug))

            syncer.sync()

            venueLinksRepo.stored.also {
                it.shouldHaveSize(1)
                it.shouldContain(2 to 1)
            }
        }
    }

    override suspend fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        api = mockk<UscApi>()
        venuesRepo = InMemoryVenueRepo()
        venueLinksRepo = InMemoryVenueLinksRepo()
        imageStorage = MemorizableImageStorage()
        syncVenueDbosAdded.clear()

        val syncDispatcher = SyncDispatcher()
        syncDispatcher.registerVenueDboAdded { syncVenueDbosAdded += it }
        syncer = VenueSyncer(
            api = api,
            venueRepo = venuesRepo,
            venueLinksRepo = venueLinksRepo,
            syncDispatcher = syncDispatcher,
            downloader = NoopDownloader,
            imageStorage = imageStorage,
            city = city,
            plan = plan,
        )
    }
}
