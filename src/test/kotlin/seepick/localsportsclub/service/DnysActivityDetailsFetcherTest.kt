package seepick.localsportsclub.service

import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.shared.DateRange
import com.github.seepick.uscclient.shared.DateTimeRange
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.persistence.venueDbo
import seepick.localsportsclub.sync.SyncProgress
import testfixtUsc.activityDetails
import testfixtUsc.dnysEvent
import java.time.LocalDateTime

class DnysActivityDetailsFetcherTest : StringSpec({
    val now = LocalDateTime.parse("2025-12-01T10:00:00")
    lateinit var venueRepo: VenueRepo
    lateinit var api: UscApi
    lateinit var progress: SyncProgress
    lateinit var fetcher: DnysActivityDetailsFetcher
    beforeTest {
        venueRepo = mockk()
        api = mockk()
        progress = mockk(relaxed = true)
        fetcher = DnysActivityDetailsFetcher(venueRepo, api, progress)
    }

    "simple case" {
        val dateTimeRange = DateTimeRange(now, now.plusHours(1))
        val venueDbo = Arb.venueDbo().next()
        val activityDbo = Arb.activityDbo().next().copy(
            teacher = null,
            venueId = venueDbo.id,
            from = dateTimeRange.from,
            to = dateTimeRange.to,
        )
        val eventTeacher = "new teacher name"
        val dnysEvent = Arb.dnysEvent().next().copy(
            dateTimeRange = dateTimeRange,
            teacher = eventTeacher,
        )
        val activityDetails = Arb.activityDetails().next().copy(
            dateTimeRange = dateTimeRange,
            teacher = null,
        )

        every { venueRepo.selectBySlug("de-nieuwe-yogaschool") } returns venueDbo
        coEvery { api.fetchDnysEvents(DateRange(now.toLocalDate(), now.toLocalDate())) } returns listOf(dnysEvent)
        fetcher.enrich(listOf(activityDbo to activityDetails)).shouldBeSingleton()
            .first().second.teacher shouldBe eventTeacher
    }

})
