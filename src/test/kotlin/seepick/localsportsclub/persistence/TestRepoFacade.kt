package seepick.localsportsclub.persistence

import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.service.model.ActivityState
import java.time.LocalDateTime

class TestRepoFacade(
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo? = null,
) {

    private var activitySequence = 1

    fun insertActivity(
        state: ActivityState = Arb.enum<ActivityState>().next(),
        from: LocalDateTime = Arb.localDateTime().next(),
        createVenue: Boolean = false,
        cityId: Int? = null,
    ): ActivityDbo {
        val venueId = if (createVenue) {
            require(venueRepo != null)
            venueRepo.insert(Arb.venueDbo().next().let { if (cityId == null) it else it.copy(cityId = cityId) }).id
        } else null

        val activity = Arb.activityDbo().next()
            .copy(
                id = activitySequence++,
                state = state,
                from = from,
                to = from.plusMinutes(30),
            ).let {
                if (venueId != null) it.copy(venueId = venueId) else it
            }
        activityRepo.insert(activity)
        return activity
    }
}
