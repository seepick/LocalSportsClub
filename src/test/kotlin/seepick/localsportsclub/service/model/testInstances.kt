package seepick.localsportsclub.service.model

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.service.date.dateTimeRange
import seepick.localsportsclub.slug

fun Arb.Companion.simpleVenue() = arbitrary {
    SimpleVenueImpl(
        id = int(min = 1).next(),
        slug = slug().next(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).next(),
        imageFileName = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().next(),
        rating = Rating.entries.random(),
        isWishlisted = boolean().next(),
        isFavorited = boolean().next(),
    )
}

fun Arb.Companion.activity() = arbitrary {
    Activity(
        id = int(min = 1).next(),
        venue = simpleVenue().next(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).next(),
        category = string(minSize = 3, maxSize = 5, codepoints = Codepoint.az()).next(),
        dateTimeRange = dateTimeRange().next(),
        spotsLeft = int(min = 0, max = 20).next(),
        isBooked = boolean().next(),
        wasCheckedin = boolean().next(),
    )
}
