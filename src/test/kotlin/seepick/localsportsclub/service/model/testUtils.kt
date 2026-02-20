package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.utils.DateTimeRange
import java.time.LocalDateTime

fun Activity.copy(
    copyDateTimeRange: DateTimeRange = dateTimeRange,
    copyState: ActivityState = state,
    copyVenue: Venue = venue,
    copyCancellationLimit: LocalDateTime? = cancellationLimit,
) = Activity(
    venue = copyVenue,
    state = copyState,
    cancellationLimit = copyCancellationLimit,
    dateTimeRange = copyDateTimeRange,
    id = id,
    name = name,
    category = category,
    spotsLeft = spotsLeft,
    teacher = teacher,
    description = description,
    plan = plan,
)

fun ActivityState.someOther() = ActivityState.entries.toSet().minus(this).random()
