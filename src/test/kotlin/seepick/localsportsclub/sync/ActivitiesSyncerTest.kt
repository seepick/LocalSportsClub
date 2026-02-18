package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activity.ActivityInfo
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.StaticClock
import seepick.localsportsclub.atAnyTime
import seepick.localsportsclub.createDaysUntil
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryVenueRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.persistence.venueDbo
import seepick.localsportsclub.sync.domain.ActivitiesSyncer
import seepick.localsportsclub.sync.domain.VenueSyncInserter
import testfixtUsc.activityInfo
import testfixtUsc.city
import testfixtUsc.plan
import java.time.LocalDateTime

class ActivitiesSyncerTest : DescribeSpec() {
    private val syncActivityAdded = mutableListOf<ActivityDbo>()
    private lateinit var api: UscApi
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var venueRepo: InMemoryVenueRepo
    private lateinit var syncerListenerDispatcher: SyncerListenerDispatcher
    private lateinit var venueSyncInserter: VenueSyncInserter
    private val todayNow = LocalDateTime.of(2024, 12, 5, 12, 0, 0)
    private val clock = StaticClock(todayNow)
    private val syncProgress = DummySyncProgress()
    private val syncDaysAhead = 4
    private val city = Arb.city().next()
    private val anyPlan = Arb.plan().next()

    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
        venueSyncInserter = mockk()
        activityRepo = InMemoryActivityRepo()
        venueRepo = InMemoryVenueRepo()
        syncerListenerDispatcher = SyncerListenerDispatcher()
        syncerListenerDispatcher.registerListener(object : TestSyncerListener() {
            override fun onActivityDbosAdded(activityDbos: List<ActivityDbo>) {
                syncActivityAdded += activityDbos
            }
        })
    }

    private fun syncer() = ActivitiesSyncer(
        api = api,
        activityRepo = activityRepo,
        venueRepo = venueRepo,
        dispatcher = syncerListenerDispatcher,
        venueSyncInserter = venueSyncInserter,
        progress = syncProgress,
    )

    init {
        describe("When full sync") {
            it("Given venue stored and activity fetched Then inserted and dispatched") {
                val venue = givenVenueStored()
                val activityInfo = givenApiReturnsActivities { copy(venueSlug = venue.slug) }

                syncWithDefaults()

                activityRepo.stored.values.shouldBeSingleton().first().should {
                    it.id shouldBe activityInfo.id
                    it.venueId shouldBe venue.id
                }
                syncActivityAdded.shouldBeSingleton().first().should {
                    it.id shouldBe activityInfo.id
                }
            }
        }
        describe("Hardening") {
            it("Given activity exists When remote get activity for another day with same ID Then simply ignore it") {
                val venue = givenVenueStored()
                val activityInfo = givenApiReturnsActivities { copy(venueSlug = venue.slug) }

                activityRepo.insert(
                    Arb.activityDbo().next().copy(
                        id = activityInfo.id, from = clock.today().minusDays(1).atAnyTime()
                    )
                )

                syncWithDefaults()

                activityRepo.stored.size shouldBe 1
            }
        }
    }

    private fun givenVenueStored(): VenueDbo {
        val venue = Arb.venueDbo().next().copy(cityId = city.id)
        venueRepo.stored[venue.id] = venue
        return venue
    }

    private fun givenApiReturnsActivities(withActivity: ActivityInfo.() -> ActivityInfo): ActivityInfo {
        val activityInfo = Arb.activityInfo().next().let(withActivity)
        coEvery {
            api.fetchActivities(any())
        } returnsMany (1..syncDaysAhead).map {
            when (it) {
                1 -> listOf(activityInfo)
                else -> emptyList()
            }
        }
        return activityInfo
    }

    private suspend fun syncWithDefaults() {
        syncer().sync(anyPlan, city, clock.today().createDaysUntil(syncDaysAhead))
    }
}
