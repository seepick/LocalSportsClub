package seepick.localsportsclub.gcal

import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDate
import java.time.LocalDateTime

fun main(args: Array<String>) {
    val calendarId = args[0]
    RealGcalService()
        .testConnection(calendarId)
//                .createDummy("wrong")
//                .deleteDummy(calendarId)
}

private fun GcalService.createDummy(calendarId: String) {
    val start = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0)
    create(
        calendarId,
        GcalEntry.GcalActivity(
            activityId = 1337,
            title = "from LSC",
            dateTimeRange = DateTimeRange(
                from = start,
                to = start.plusHours(1),
            ),
            location = "location",
            notes = "some notes",
        )
    )
}

private fun GcalService.deleteDummy(calendarId: String) {
    delete(
        calendarId,
        GcalDeletion(
            day = LocalDate.now(),
            activityOrFreetrainingId = 1337,
            isActivity = true,
        )
    )
}
