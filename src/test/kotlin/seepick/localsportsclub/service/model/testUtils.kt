package seepick.localsportsclub.service.model

import seepick.localsportsclub.service.date.DateTimeRange

fun Activity.copy(
    copyDateTimeRange: DateTimeRange = dateTimeRange,
    copyIsBooked: Boolean = isBooked,
    copyWasCheckedin: Boolean = wasCheckedin,
) =
    Activity(
        id = id,
        venue = venue,
        name = name,
        category = category,
        dateTimeRange = copyDateTimeRange,
        spotsLeft = spotsLeft,
        teacher = teacher,
        isBooked = copyIsBooked,
        wasCheckedin = copyWasCheckedin,
    )
