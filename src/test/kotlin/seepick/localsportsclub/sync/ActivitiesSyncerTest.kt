package seepick.localsportsclub.sync

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.TestableClock
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.activityInfo
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryVenueRepo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.persistence.venueDbo
import seepick.localsportsclub.uscConfig
import java.time.LocalDateTime

class ActivitiesSyncerTest : DescribeSpec() {
    private val syncActivityAdded = mutableListOf<ActivityDbo>()
    private lateinit var api: UscApi
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var venueRepo: InMemoryVenueRepo
    private lateinit var syncerListenerDispatcher: SyncerListenerDispatcher
    private lateinit var venueSyncInserter: VenueSyncInserter
    private val todayNow = LocalDateTime.of(2024, 12, 5, 12, 0, 0)
    private val clock = TestableClock(todayNow)
    private val syncDaysAhead = 4

    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
        venueSyncInserter = mockk()
        activityRepo = InMemoryActivityRepo()
        venueRepo = InMemoryVenueRepo()
        syncerListenerDispatcher = SyncerListenerDispatcher()
        syncerListenerDispatcher.registerListener(object : TestSyncerListener() {
            override fun onActivityDboAdded(activityDbo: ActivityDbo) {
                syncActivityAdded += activityDbo
            }
        })
        clock.setNowAndToday(LocalDateTime.now())
    }

    private fun syncer(daysAhead: Int) = ActivitiesSyncer(
        api = api,
        activityRepo = activityRepo,
        venueRepo = venueRepo,
        clock = clock,
        dispatcher = syncerListenerDispatcher,
        venueSyncInserter = venueSyncInserter,
        uscConfig = Arb.uscConfig().next().copy(syncDaysAhead = daysAhead),
    )

    init {
        describe("When full sync") {
            it("Given venue stored and activity fetched Then inserted and dispatched") {
                val venue = Arb.venueDbo().next()
                val activityInfo = Arb.activityInfo().next().copy(venueSlug = venue.slug)
                venueRepo.stored[venue.id] = venue
                coEvery {
                    api.fetchActivities(any())
                } returnsMany (1..syncDaysAhead).map {
                    when (it) {
                        1 -> listOf(activityInfo)
                        else -> emptyList()
                    }
                }
                syncer(syncDaysAhead).sync()

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

    private fun insertActivity(date: LocalDateTime) {
        activityRepo.insert(Arb.activityDbo().next().copy(from = date))
    }
}
