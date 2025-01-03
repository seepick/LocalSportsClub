package seepick.localsportsclub.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.mockk
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.api.checkin.CheckinEntry
import seepick.localsportsclub.api.checkin.CheckinsPage
import seepick.localsportsclub.api.checkinEntry
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.InMemoryActivityRepo
import seepick.localsportsclub.persistence.TestRepoFacade
import seepick.localsportsclub.persistence.activityDbo
import java.time.LocalDate
import java.time.LocalDateTime

class CheckinSyncerTest : StringSpec() {

    private lateinit var activityRepo: InMemoryActivityRepo
    private lateinit var syncerListenerDispatcher: SyncerListenerDispatcher
    private lateinit var uscApi: UscApi
    private lateinit var dataSyncRescuer: DataSyncRescuer
    private lateinit var syncer: CheckinSyncer
    private lateinit var syncActivityDbosUpdated: MutableList<Pair<ActivityDbo, ActivityFieldUpdate>>
    private lateinit var testRepo: TestRepoFacade
    private val now = LocalDateTime.now()
    private val today = now.toLocalDate()

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
        testRepo = TestRepoFacade(activityRepo)
        syncer = CheckinSyncer(
            uscApi = uscApi,
            activityRepo = activityRepo,
            dispatcher = syncerListenerDispatcher,
            dataSyncRescuer = dataSyncRescuer
        )
    }


    private fun mockCheckinsPage(pageNr: Int, date: LocalDate, activityId: Int): CheckinEntry {
        val entry = Arb.checkinEntry().next().copy(date = date, activityId = activityId)
        coEvery { uscApi.fetchCheckinsPage(pageNr) } returns CheckinsPage(listOf(entry))
        return entry
    }

    private fun mockCheckinsEmptyPage(pageNr: Int) {
        coEvery { uscApi.fetchCheckinsPage(pageNr) } returns CheckinsPage.empty
    }

    init {
        "Given non-checkedin activity and page with entry for it Then update it" {
            val activity = testRepo.insertActivity(wasCheckedin = false)
            val entry = Arb.checkinEntry().next().copy(activityId = activity.id)
            coEvery { uscApi.fetchCheckinsPage(1) } returns CheckinsPage(listOf(entry))
            mockCheckinsEmptyPage(2)

            syncer.sync()

            val expected = activity.copy(wasCheckedin = true)
            activityRepo.selectById(activity.id) shouldBe expected
            syncActivityDbosUpdated.shouldBeSingleton().first() shouldBe (expected to ActivityFieldUpdate.WasCheckedin)
        }
        "Given local checked-in activity Then sync until that time although more pages potentially available" {
            val activity1 = testRepo.insertActivity(wasCheckedin = false, from = now.minusDays(5))
            val activity2 = testRepo.insertActivity(wasCheckedin = false, from = now.minusDays(5))
            val activity3 = testRepo.insertActivity(wasCheckedin = false, from = now.minusDays(5))
            testRepo.insertActivity(wasCheckedin = true, from = now.minusDays(1)) // pivot activity

            mockCheckinsPage(1, today, activity1.id)
            mockCheckinsPage(2, today.minusDays(1), activity2.id)
            mockCheckinsPage(3, today.minusDays(2), activity3.id)
            // no further mocking of pages as interrupted by pivot activity

            syncer.sync()
        }
        "Given checkin for locally non-existing activity Then refetch and rescue it" {
            val nonExistingActivityId = 42
            val entry = mockCheckinsPage(1, today, nonExistingActivityId)
            val rescuedActivity = Arb.activityDbo().next().copy(wasCheckedin = false)
            mockCheckinsEmptyPage(2)
            coEvery { dataSyncRescuer.rescueActivity(nonExistingActivityId, entry.venueSlug, any()) } answers {
                activityRepo.insert(rescuedActivity)
                rescuedActivity
            }

            syncer.sync()

            val expected = rescuedActivity.copy(wasCheckedin = true)
            activityRepo.selectById(expected.id) shouldBe expected
            syncActivityDbosUpdated.shouldBeSingleton().first() shouldBe (expected to ActivityFieldUpdate.WasCheckedin)
        }
    }

}
