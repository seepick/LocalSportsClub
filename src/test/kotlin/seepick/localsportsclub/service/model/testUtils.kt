package seepick.localsportsclub.service.model

import seepick.localsportsclub.service.date.DateTimeRange
import java.time.LocalDateTime

fun Activity.copy(
    copyDateTimeRange: DateTimeRange = dateTimeRange,
    copyState: ActivityState = state,
    copyVenue: Venue = venue,
    copyCancellationLimit: LocalDateTime? = cancellationLimit,
) = Activity(
    id = id,
    venue = copyVenue,
    name = name,
    category = category,
    dateTimeRange = copyDateTimeRange,
    spotsLeft = spotsLeft,
    teacher = teacher,
    state = copyState,
    cancellationLimit = copyCancellationLimit,
)

fun ActivityState.someOther() = ActivityState.entries.toSet().minus(this).random()
