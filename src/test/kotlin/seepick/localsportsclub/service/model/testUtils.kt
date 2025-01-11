package seepick.localsportsclub.service.model

import seepick.localsportsclub.service.date.DateTimeRange

fun Activity.copy(
    copyDateTimeRange: DateTimeRange = dateTimeRange,
    copyState: ActivityState = state,
) = Activity(
    id = id,
    venue = venue,
    name = name,
    category = category,
    dateTimeRange = copyDateTimeRange,
    spotsLeft = spotsLeft,
    teacher = teacher,
    state = copyState,
)

fun ActivityState.someOther() = ActivityState.entries.toSet().minus(this).random()
