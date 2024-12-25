package seepick.localsportsclub.persistence

import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.next
import java.time.LocalDateTime

class TestRepoFacade(
    private val activityRepo: ActivityRepo,
    private val venueRepo: VenueRepo? = null,
) {

    private var activitySequence = 1

    fun insertActivity(
        wasCheckedin: Boolean = Arb.boolean().next(),
        isBooked: Boolean = Arb.boolean().next(),
        from: LocalDateTime = Arb.localDateTime().next(),
        createVenue: Boolean = false,
    ): ActivityDbo {
        val venueId = if (createVenue) {
            require(venueRepo != null)
            venueRepo.insert(Arb.venueDbo().next()).id
        } else null

        val activity = Arb.activityDbo().next()
            .copy(
                id = activitySequence++,
                isBooked = isBooked,
                wasCheckedin = wasCheckedin,
                from = from,
                to = from.plusMinutes(30),
            ).let {
                if (venueId != null) it.copy(venueId = venueId) else it
            }
        activityRepo.insert(activity)
        return activity
    }
}
