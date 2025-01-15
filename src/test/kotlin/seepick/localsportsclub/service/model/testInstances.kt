package seepick.localsportsclub.service.model

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.api.City
import seepick.localsportsclub.service.date.dateTimeRange
import seepick.localsportsclub.slug

fun Arb.Companion.venue() = arbitrary {
    Venue(
        id = int(min = 1).next(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).next(),
        slug = slug().next(),
        description = string(minSize = 5, maxSize = 20).next(),
        notes = string(minSize = 5, maxSize = 20).next(),
        categories = emptyList(),
        city = City.entries.random(),
        postalCode = "",
        addressLocality = "",
        street = "",
        location = null,
        distanceInKm = null,
        imageFileName = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().next(),
        importantInfo = null,
        openingTimes = null,
        uscWebsite = "",
        officialWebsite = null,
        isDeleted = boolean().next(),
        isFavorited = boolean().next(),
        isWishlisted = boolean().next(),
        isHidden = boolean().next(),
        rating = Rating.entries.random(),
    )
}

fun Arb.Companion.activity() = arbitrary {
    Activity(
        id = int(min = 1).next(),
        venue = venue().next(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).next(),
        category = string(minSize = 3, maxSize = 5, codepoints = Codepoint.az()).next(),
        dateTimeRange = dateTimeRange().next(),
        spotsLeft = int(min = 0, max = 20).next(),
        teacher = string(minSize = 2, maxSize = 25, codepoints = Codepoint.az()).orNull().next(),
        state = enum<ActivityState>().next(),
    )
}
