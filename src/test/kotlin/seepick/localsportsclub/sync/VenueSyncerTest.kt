package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.venue.VenuesFilter
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.imageUrl
import seepick.localsportsclub.persistence.DbListener
import seepick.localsportsclub.persistence.InMemoryVenueLinksRepo
import seepick.localsportsclub.persistence.InMemoryVenueRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueIdLink
import seepick.localsportsclub.persistence.venueDbo
import seepick.localsportsclub.service.DummyFileResolver
import seepick.localsportsclub.service.MemorizableImageStorage
import seepick.localsportsclub.sync.domain.VenueSyncInserterImpl
import seepick.localsportsclub.sync.domain.VenueSyncer
import testfixtUsc.city
import testfixtUsc.plan
import testfixtUsc.venueDetails
import testfixtUsc.venueInfo

class VenueSyncerTest : StringSpec() {

    private val remoteVenue = Arb.venueInfo().next()
    private val remoteDetails = Arb.venueDetails().next()
    private val city = Arb.city().next()
    private val plan = Arb.plan().next()
    private val syncVenueDbosAdded = mutableListOf<VenueDbo>()
    private val syncVenueDbosMarkedDeleted = mutableListOf<VenueDbo>()
    private lateinit var api: UscApi
    private lateinit var venueRepo: InMemoryVenueRepo
    private lateinit var venueLinksRepo: InMemoryVenueLinksRepo
    private lateinit var imageStorage: MemorizableImageStorage
    private lateinit var syncer: VenueSyncer

    override suspend fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        api = mockk<UscApi>()
        venueRepo = InMemoryVenueRepo()
        venueLinksRepo = InMemoryVenueLinksRepo()
        imageStorage = MemorizableImageStorage()
        syncVenueDbosAdded.clear()
        syncVenueDbosMarkedDeleted.clear()

        val syncerListenerDispatcher = SyncerListenerDispatcher()
        syncerListenerDispatcher.registerListener(object : TestSyncerListener() {
            override fun onVenueDbosAdded(venueDbos: List<VenueDbo>) {
                syncVenueDbosAdded += venueDbos
            }

            override fun onVenueDbosMarkedDeleted(venueDbos: List<VenueDbo>) {
                syncVenueDbosMarkedDeleted += venueDbos
            }
        })
        val syncProgress = DummySyncProgress()
        syncer = VenueSyncer(
            uscApi = api,
            venueRepo = venueRepo,
            dispatcher = syncerListenerDispatcher,
            venueSyncInserter = VenueSyncInserterImpl(
                api,
                venueRepo,
                venueLinksRepo,
                NoopDownloader,
                imageStorage,
                syncerListenerDispatcher,
                syncProgress,
                DummyFileResolver,
            ),
            progress = syncProgress,
        )
    }

    init {
        extension(DbListener())

        "Given api returns 1 and db has 0 When sync Then inserted, synced, and image saved" {
            val imageUrl = Arb.imageUrl().next()
            coEvery {
                api.fetchVenues(VenuesFilter(city, plan), any())
            } returns listOf(remoteVenue.copy(imageUrl = imageUrl))
            coEvery { api.fetchVenueDetail(eq(remoteVenue.slug)) } returns
                    remoteDetails.copy(linkedVenueSlugs = emptyList(), originalImageUrl = Arb.imageUrl().next())

            syncer.sync(plan, city)

            venueRepo.stored.should {
                val stored = it.values.shouldBeSingleton().first()
                // can't test on ID
                stored.name shouldBe remoteDetails.title
                stored.slug shouldBe remoteDetails.slug

                syncVenueDbosAdded.shouldBeSingleton().first().slug shouldBe remoteDetails.slug
                val expectedImageFileName = "${stored.slug}.png"
                imageStorage.savedVenueImages.shouldBeSingleton().first().first shouldBe expectedImageFileName
            }
            syncVenueDbosMarkedDeleted.shouldBeEmpty()
        }
        "Given api returns 0 and db has 1 When sync Then mark as deleted" {
            coEvery { api.fetchVenues(VenuesFilter(city, plan), any()) } returns emptyList()
            val stored = venueRepo.insert(Arb.venueDbo().next().copy(cityId = city.id, isDeleted = false))

            syncer.sync(plan, city)

            venueRepo.stored.values.shouldBeSingleton().first().isDeleted shouldBe true
            syncVenueDbosMarkedDeleted.shouldBeSingleton().first().id shouldBe stored.id
        }
        "Given 1 stored And api returns it and one linked to it When sync Then link them" {
            val stored = venueRepo.insert(Arb.venueDbo().next().copy(cityId = city.id, isDeleted = false))
            coEvery { api.fetchVenues(VenuesFilter(city, plan), any()) } returns listOf(
                remoteVenue.copy(slug = stored.slug), remoteVenue
            )
            coEvery { api.fetchVenueDetail(remoteVenue.slug) } returns remoteDetails
                .copy(slug = remoteVenue.slug, linkedVenueSlugs = listOf(stored.slug))

            syncer.sync(plan, city)

            venueLinksRepo.stored.also {
                it.shouldHaveSize(1)
                it.shouldContain(VenueIdLink(2, 1))
            }
            syncVenueDbosMarkedDeleted.shouldBeEmpty()
        }
    }
}
