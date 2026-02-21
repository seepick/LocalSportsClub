package seepick.localsportsclub.gcal

import seepick.localsportsclub.service.singles.SinglesService

fun SinglesService.readCalendarIdOrThrow() =
    preferences.gcal.maybeCalendarId ?: error("No calendar ID set!")
