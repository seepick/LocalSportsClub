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
import seepick.localsportsclub.api.activityInfo
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryVenueRepo
import seepick.localsportsclub.persistence.testInfra.venueDbo

class ActivitiesSyncerTest : StringSpec() {
    private val city = City.Amsterdam
    private val plan = PlanType.Medium
    private val syncActivityAdded = mutableListOf<ActivityDbo>()
    private lateinit var api: UscApi
    private lateinit var syncer: ActivitiesSyncer
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var venueRepo: InMemoryVenueRepo
    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
        activityRepo = InMemoryActivityRepo()
        venueRepo = InMemoryVenueRepo()
        val syncDispatcher = SyncDispatcher()
        syncDispatcher.registerActivityAdded { syncActivityAdded += it }
        syncer = ActivitiesSyncer(
            api = api,
            city = city,
            plan = plan,
            syncDispatcher = syncDispatcher,
            activityRepo = activityRepo,
            venueRepo = venueRepo,
        )
    }

    init {
        "Given venue and activity fetched When sync Then inserted and dispatched" {
            val venue = Arb.venueDbo().next()
            val activityInfo = Arb.activityInfo().next().copy(venueSlug = venue.slug)
            venueRepo.stored[venue.id] = venue
            coEvery {
                api.fetchActivities(any())
            } returnsMany (1..14).map {
                when (it) {
                    3 -> listOf(activityInfo)
                    else -> emptyList()
                }
            }
            syncer.sync()

            activityRepo.stored.values.shouldBeSingleton().first().should {
                it.id shouldBe activityInfo.id
                it.venueId shouldBe venue.id
            }
            syncActivityAdded.shouldBeSingleton().first().should {
                it.id shouldBe activityInfo.id
            }
        }
    }
}
