package seepick.localsportsclub.tools

import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.persistence.cliConnectToDatabase

object BugfixDataCleaner {

    private val activityRepo: ActivityRepo = ExposedActivityRepo
    private val venueRepo: VenueRepo = ExposedVenueRepo

    @JvmStatic
    fun main(args: Array<String>) {
        cliConnectToDatabase(isProd = false)
        runBlocking {
            fixMovementAmsterdamTeacher()
        }
    }

    suspend fun fixMovementAmsterdamTeacher() {
        val venue = venueRepo.selectBySlug("movement-amsterdam")!!
//        val events = EversportsFetcher(httpClient, NoopResponseStorage, SystemClock).fetch(
//            EversportsFetchRequestImpl(
//                eversportsId = "J3pkIl",
//                slug = "movement-amsterdam",
//            )
//        )
        activityRepo.selectAllForVenueId(venue.id).forEach { activity ->
            println(activity)
//            val updateTeacher = events.firstOrNull {
//                it.dateTimeRange.from == activity.from && it.dateTimeRange.to == activity.to
//            }?.teacher
//            println("UPDATING: ${activity.name} @ ${activity.from} [${activity.teacher}] => [${updateTeacher}]")
//            val updated = activity.copy(teacher = updateTeacher)
//            activityRepo.update(updated)
        }
    }
}

