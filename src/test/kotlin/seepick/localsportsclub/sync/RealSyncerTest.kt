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
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.venueInfo
import seepick.localsportsclub.persistence.InMemoryVenuesRepo


class RealSyncerTest : StringSpec() {

    private val remoteVenue = Arb.venueInfo().next()

    private lateinit var api: UscApi
    private lateinit var venuesRepo: InMemoryVenuesRepo
    private lateinit var syncer: RealSyncer

    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
        venuesRepo = InMemoryVenuesRepo()
        syncer = RealSyncer(api, venuesRepo)
    }

    init {
        "Given api returns 1 and db has 0 When sync Then insert it" {
            coEvery { api.fetchVenues() } returns listOf(remoteVenue)

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
