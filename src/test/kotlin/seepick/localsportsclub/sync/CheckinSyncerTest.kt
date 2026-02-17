package seepick.localsportsclub.sync

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.activityCheckinEntry
import com.github.seepick.uscclient.checkin.ActivityCheckinEntryType
import com.github.seepick.uscclient.checkin.CheckinEntry
import com.github.seepick.uscclient.checkin.CheckinsPage
import com.github.seepick.uscclient.city
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.InMemoryFreetrainingRepo
import seepick.localsportsclub.persistence.TestRepoFacade
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.service.date.SystemClock
import seepick.localsportsclub.service.model.ActivityState
import java.time.LocalDate

class CheckinSyncerTest : StringSpec() {

    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var freetrainingRepo: InMemoryFreetrainingRepo
    private lateinit var syncerListenerDispatcher: SyncerListenerDispatcher
    private lateinit var uscApi: UscApi
    private lateinit var dataSyncRescuer: DataSyncRescuer
    private lateinit var syncer: CheckinSyncer
    private lateinit var syncActivityDbosUpdated: MutableList<Pair<ActivityDbo, ActivityFieldUpdate>>
    private lateinit var testRepo: TestRepoFacade
    private val now = SystemClock.now()
    private val today = now.toLocalDate()
    private val city = Arb.city().next()


    override suspend fun beforeEach(testCase: TestCase) {
        uscApi = mockk()
        dataSyncRescuer = mockk()

        syncerListenerDispatcher = SyncerListenerDispatcher()
        syncActivityDbosUpdated = mutableListOf()
        syncerListenerDispatcher.registerListener(object : TestSyncerListener() {
            override fun onActivityDboUpdated(activityDbo: ActivityDbo, field: ActivityFieldUpdate) {
                syncActivityDbosUpdated += activityDbo to field
            }
        })

        activityRepo = InMemoryActivityRepo()
        freetrainingRepo = InMemoryFreetrainingRepo()
        testRepo = TestRepoFacade(activityRepo)
        syncer = CheckinSyncer(
            uscApi = uscApi,
            activityRepo = activityRepo,
            freetrainingRepo = freetrainingRepo,
            dispatcher = syncerListenerDispatcher,
            dataSyncRescuer = dataSyncRescuer,
            progress = DummySyncProgress,
            clock = SystemClock,
        )
    }


    private fun mockCheckinsPage(
        pageNr: Int,
        date: LocalDate,
        activityId: Int,
        type: ActivityCheckinEntryType = ActivityCheckinEntryType.Checkedin,
    ): CheckinEntry {
        val entry = Arb.activityCheckinEntry().next()
            .copy(
                activityId = activityId,
                date = date,
                type = type,
            )
        coEvery { uscApi.fetchCheckinsPage(pageNr, today) } returns CheckinsPage(listOf(entry))
        return entry
    }

    private fun mockCheckinsEmptyPage(pageNr: Int, today: LocalDate) {
        coEvery { uscApi.fetchCheckinsPage(pageNr, today) } returns CheckinsPage.empty
    }

    init {
        "Given non-checkedin activity and page with entry for it Then update it" {
            val activity = testRepo.insertActivity(state = ActivityState.Blank)
            val entry = Arb.activityCheckinEntry().next()
                .copy(activityId = activity.id, type = ActivityCheckinEntryType.Checkedin)
            coEvery { uscApi.fetchCheckinsPage(1, today) } returns CheckinsPage(listOf(entry))
            mockCheckinsEmptyPage(2, today)

            syncer.sync(city)

            val expected = activity.copy(state = ActivityState.Checkedin)
            activityRepo.selectById(activity.id) shouldBe expected
            syncActivityDbosUpdated.shouldBeSingleton().first() shouldBe (expected to ActivityFieldUpdate.State(
                ActivityState.Blank
            ))
        }
        "Given local checked-in activity Then sync until that time although more pages potentially available" {
            val activity1 = testRepo.insertActivity(state = ActivityState.Blank, from = now.minusDays(5))
            val activity2 = testRepo.insertActivity(state = ActivityState.Blank, from = now.minusDays(5))
            val activity3 = testRepo.insertActivity(state = ActivityState.Blank, from = now.minusDays(5))
            testRepo.insertActivity(state = ActivityState.Checkedin, from = now.minusDays(1)) // pivot activity

            mockCheckinsPage(1, today, activity1.id)
            mockCheckinsPage(2, today.minusDays(1), activity2.id)
            mockCheckinsPage(3, today.minusDays(2), activity3.id)
            // no further mocking of pages as interrupted by pivot activity

            syncer.sync(city)
        }
        "Given checkin for locally non-existing activity Then refetch and rescue it" {
            val nonExistingActivityId = 42
            val checkinEntry = mockCheckinsPage(1, today, nonExistingActivityId)
            mockCheckinsEmptyPage(2, today)
            val rescuedActivity = Arb.activityDbo().next().copy(state = ActivityState.Blank)
            coEvery {
                dataSyncRescuer.fetchInsertAndDispatchActivity(
                    city,
                    nonExistingActivityId,
                    checkinEntry.venueSlug,
                    any()
                )
            } answers {
                activityRepo.insert(rescuedActivity)
                rescuedActivity
            }

            syncer.sync(city)

            val expected = rescuedActivity.copy(state = ActivityState.Checkedin)
            activityRepo.selectById(expected.id) shouldBe expected
            syncActivityDbosUpdated.shouldBeSingleton().first() shouldBe (expected to ActivityFieldUpdate.State(
                ActivityState.Blank
            ))
        }
        listOf(
            ActivityState.Checkedin to ActivityCheckinEntryType.Checkedin,
            ActivityState.CancelledLate to ActivityCheckinEntryType.CancelledLate,
            ActivityState.Noshow to ActivityCheckinEntryType.Noshow,
        ).forEach { (state, type) ->
            "Given activity already ${state.name} When get it again Then do nothing" {
                val activity = testRepo.insertActivity(state = state)
                mockCheckinsPage(1, today, activity.id, type)
                mockCheckinsEmptyPage(2, today)

                syncer.sync(city)

                activityRepo.selectById(activity.id) shouldBe activity
                syncActivityDbosUpdated.shouldBeEmpty()
            }
        }
    }

}
