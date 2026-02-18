package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.service.date.Clock

class VenueService(
    private val activityRepo: ActivityRepo,
    private val activityDetailService: ActivityDetailService,
    private val clock: Clock,
) {
    private val log = logger {}

    suspend fun syncActivityDetails(venueId: Int) {
        log.debug { "syncActivityDetails(venueId=$venueId)" }
        val now = clock.now()
        activityDetailService.syncBulk(
            activityIds = activityRepo.selectAllForVenueId(venueId)
                .filter { it.from >= now }
                .map { it.id }
        )
    }
}
