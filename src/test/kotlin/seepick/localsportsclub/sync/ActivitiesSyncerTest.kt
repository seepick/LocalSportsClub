package seepick.localsportsclub.sync

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
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
import seepick.localsportsclub.persistence.testInfra.activityDbo
import seepick.localsportsclub.persistence.testInfra.venueDbo
import seepick.localsportsclub.uscConfig
import java.time.LocalDateTime

class ActivitiesSyncerTest : DescribeSpec() {
    private val uscConfig = Arb.uscConfig().next()
    private val syncActivityAdded = mutableListOf<ActivityDbo>()
    private lateinit var api: UscApi
    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var venueRepo: InMemoryVenueRepo
    private lateinit var syncerListenerDispatcher: SyncerListenerDispatcher
    private val todayNow = LocalDateTime.of(2024, 12, 5, 12, 0, 0)
    private val clock = TestableClock(todayNow)
    private val syncDaysAhead = 4
    
    override suspend fun beforeEach(testCase: TestCase) {
        api = mockk<UscApi>()
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

    private fun syncer(daysAhead: Int = syncDaysAhead) = ActivitiesSyncer(
        api = api,
        activityRepo = activityRepo,
        venueRepo = venueRepo,
        clock = clock,
        syncDaysAhead = daysAhead,
        dispatcher = syncerListenerDispatcher,
        uscConfig = uscConfig,
    )

    init {
        describe("When full sync") {
            it("Given venue stored and activity fetched Then inserted and dispatched") {
                val venue = Arb.venueDbo().next()
                val activityInfo = Arb.activityInfo().next().copy(venueSlug = venue.slug)
                venueRepo.stored[venue.id] = venue
                coEvery {
                    api.fetchActivities(any())
                } returnsMany (1..syncDaysAhead).map { // TODO need to rewrite test once syncer changed
                    when (it) {
                        1 -> listOf(activityInfo)
                        else -> emptyList()
                    }
                }
                syncer().sync()

                activityRepo.stored.values.shouldBeSingleton().first().should {
                    it.id shouldBe activityInfo.id
                    it.venueId shouldBe venue.id
                }
                syncActivityAdded.shouldBeSingleton().first().should {
                    it.id shouldBe activityInfo.id
                }
            }
        }
        describe("When get days to sync") {
            /*
            today: 5.12.
            daysToSync = 4 -> max = 8.12.

            futurest: null  -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 1.12. -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 4.12. -> [ 5.12., 6.12., 7.12., 8.12. ]
            futurest: 5.12. -> [ 6.12., 7.12., 8.12. ]
            futurest: 6.12. -> [ 7.12., 8.12. ]
            futurest: 7.12. -> [ 8.12. ]
            futurest: 8.12. -> [ ]
            futurest: 9.12. -> [ ]
             */
            it("Given nothing Then return max") {
                clock.setNowAndToday(todayNow)
                syncer(syncDaysAhead).daysToSync()
                    .shouldBe((1..syncDaysAhead).map { todayNow.plusDays(it - 1L).toLocalDate() })
            }
            it("Given way in past Then return max") {
                insertActivity(todayNow.minusDays(42))
                clock.setNowAndToday(todayNow)
                syncer(syncDaysAhead).daysToSync().shouldHaveSize(syncDaysAhead)
            }
            it("Given one day ahead Then return max minus one") {
                insertActivity(todayNow.plusDays(1))
                clock.setNowAndToday(todayNow)
                syncer(syncDaysAhead).daysToSync().shouldHaveSize(syncDaysAhead - 1)
            }
            it("Given almost but two ahead Then return one") {
                insertActivity(todayNow.plusDays(syncDaysAhead - 1L))
                clock.setNowAndToday(todayNow)
                syncer(syncDaysAhead).daysToSync().shouldBeSingleton()
                    .first() shouldBe todayNow.plusDays(syncDaysAhead - 1L).toLocalDate()
            }
            it("Given overly ahead Then return empty") {
                insertActivity(todayNow.plusDays(syncDaysAhead.toLong()))
                clock.setNowAndToday(todayNow)
                syncer(syncDaysAhead).daysToSync().shouldBeEmpty()
            }
        }
    }

    private fun insertActivity(date: LocalDateTime) {
        activityRepo.insert(Arb.activityDbo().next().copy(from = date))
    }
}
