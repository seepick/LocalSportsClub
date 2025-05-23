package seepick.localsportsclub.api

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.api.activity.ActivityInfo
import seepick.localsportsclub.api.checkin.ActivityCheckinEntry
import seepick.localsportsclub.api.checkin.ActivityCheckinEntryType
import seepick.localsportsclub.api.checkin.FreetrainingCheckinEntry
import seepick.localsportsclub.api.venue.VenueDetails
import seepick.localsportsclub.api.venue.VenueInfo
import seepick.localsportsclub.imageUrl
import seepick.localsportsclub.service.date.dateTimeRange
import seepick.localsportsclub.service.date.timeRange
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.slug
import seepick.localsportsclub.url

fun Arb.Companion.venueInfo() = arbitrary {
    VenueInfo(
        title = string(minSize = 3, maxSize = 30, codepoints = Codepoint.alphanumeric()).next(),
        slug = string(minSize = 3, maxSize = 6, codepoints = Codepoint.alphanumeric()).next(),
        imageUrl = imageUrl().orNull().next(),
        disciplines = list(string(minSize = 3, maxSize = 8, codepoints = Codepoint.az()), 0..3).next(),
        addressId = int(min = 1, max = 999).next(),
        addressDistrict = string(minSize = 3, maxSize = 5, codepoints = Codepoint.alphanumeric()).next(),
        addressStreet = string(minSize = 5, maxSize = 10, codepoints = Codepoint.alphanumeric()).next(),
        plan = enum<Plan.UscPlan>().next(),
    )
}

fun Arb.Companion.venueDetails() = arbitrary {
    VenueDetails(
        title = string(minSize = 5, maxSize = 15, codepoints = Codepoint.az()).next(),
        slug = string(minSize = 3, maxSize = 5, codepoints = Codepoint.az()).next(),
        linkedVenueSlugs = list(slug(), 0..3).next(),
        websiteUrl = url().orNull().next(),
        description = string(minSize = 3, maxSize = 50).next(),
        importantInfo = string(minSize = 3, maxSize = 50).orNull().next(),
        openingTimes = string(minSize = 3, maxSize = 50).orNull().next(),
        disciplines = list(string(minSize = 3, maxSize = 6), 0..3).next(),
        longitude = string(minSize = 3, maxSize = 50).next(),
        latitude = string(minSize = 3, maxSize = 50).next(),
        postalCode = string(minSize = 3, maxSize = 50).next(),
        addressLocality = string(minSize = 3, maxSize = 50).next(),
        streetAddress = string(minSize = 3, maxSize = 50).next(),
        originalImageUrl = url().orNull().next(),
    )
}

fun Arb.Companion.activityInfo() = arbitrary {
    ActivityInfo(
        id = int(min = 1).next(),
        venueSlug = slug().next(),
        name = string(minSize = 5, maxSize = 20).next(),
        category = string(minSize = 1, maxSize = 5, codepoints = Codepoint.az()).next(),
        spotsLeft = int(min = 0, max = 10).next(),
        dateTimeRange = dateTimeRange().next(),
        plan = enum<Plan.UscPlan>().next(),
    )
}

fun Arb.Companion.activityCheckinEntry() = arbitrary {
    ActivityCheckinEntry(
        activityId = int(min = 1).next(),
        venueSlug = slug().next(),
        date = localDate().next(),
        timeRange = timeRange().next(),
        type = enum<ActivityCheckinEntryType>().next(),
    )
}

fun Arb.Companion.freetrainingCheckinEntry() = arbitrary {
    FreetrainingCheckinEntry(
        freetrainingId = int(min = 1).next(),
        venueSlug = slug().next(),
        date = localDate().next(),
    )
}

fun Arb.Companion.phpSessionId() = arbitrary {
    PhpSessionId(value = string().next())
}
