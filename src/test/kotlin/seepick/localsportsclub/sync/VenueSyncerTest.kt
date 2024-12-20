package seepick.localsportsclub.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
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
import seepick.localsportsclub.persistence.InMemoryVenueLinksRepo
import seepick.localsportsclub.persistence.InMemoryVenuesRepo
import seepick.localsportsclub.persistence.testInfra.DbListener


class VenueSyncerTest : StringSpec() {

    private val remoteVenue = Arb.venueInfo().next()
    private val remoteDetails = Arb.venueDetails().next()
    private lateinit var api: UscApi
    private lateinit var venuesRepo: InMemoryVenuesRepo
    private lateinit var venueLinksRepo: InMemoryVenueLinksRepo
    private lateinit var syncer: VenueSyncer
    private val city = City.entries.random()
    private val plan = PlanType.entries.random()

    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
        venuesRepo = InMemoryVenuesRepo()
        venueLinksRepo = InMemoryVenueLinksRepo()

        syncer = VenueSyncer(api, venuesRepo, venueLinksRepo, city, plan)
    }

    init {
        extension(DbListener())

        "Given api returns 1 and db has 0 When sync Then insert it" {
            coEvery { api.fetchVenues(eq(VenuesFilter(city, plan))) } returns listOf(remoteVenue)
            coEvery { api.fetchVenueDetail(eq(remoteVenue.slug)) } returns remoteDetails.copy(linkedVenueSlugs = emptyList())

            syncer.sync()

            venuesRepo.stored.should {
                val stored = it.values.shouldBeSingleton().first()
                // can't test on ID
                stored.name shouldBe remoteVenue.title
                stored.slug shouldBe remoteVenue.slug
            }
        }
    }
}
