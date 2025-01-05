package seepick.localsportsclub.persistence

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.venueDbo() = arbitrary {
    val id = int(min = 1).next()
    VenueDbo(
        id = id,
        name = string(minSize = 3, maxSize = 20, codepoints = Codepoint.alphanumeric()).next(),
        slug = string(minSize = 3, maxSize = 8, codepoints = Codepoint.alphanumeric()).next(),
        facilities = string(minSize = 0, maxSize = 20, codepoints = Codepoint.alphanumeric()).next(),
        cityId = int(min = 1).next(),
        officialWebsite = string(minSize = 5, maxSize = 20, codepoints = Codepoint.alphanumeric()).orNull().next(),
        rating = 0,
        description = string(minSize = 5, maxSize = 50).next(),
        openingTimes = string(minSize = 5, maxSize = 50).orNull().next(),
        importantInfo = string(minSize = 5, maxSize = 50).orNull().next(),
        imageFileName = if (boolean().next()) "$id.png" else null,
        notes = string(minSize = 0, maxSize = 20, codepoints = Codepoint.alphanumeric()).next(),
        isFavorited = boolean().next(),
        isWishlisted = boolean().next(),
        isHidden = boolean().next(),
        isDeleted = boolean().next(),
        addressLocality = string(minSize = 5, maxSize = 50).next(),
        street = string(minSize = 5, maxSize = 50).next(),
        postalCode = string(minSize = 5, maxSize = 6).next(),
        longitude = double(0.0, 5.0, false).next().toString().take(5),
        latitude = double(0.0, 5.0, false).next().toString().take(5),
    )
}

fun Arb.Companion.activityDbo() = arbitrary {
    val from = localDateTime().next()
    ActivityDbo(
        id = int(min = 1).next(),
        venueId = int(min = 1).next(),
        name = string(minSize = 5, maxSize = 20).next(),
        category = category().next(),
        spotsLeft = int(min = 0, max = 10).next(),
        from = from,
        to = from.plusMinutes(long(min = 30, max = 120).next()),
        teacher = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().next(),
        isBooked = boolean().next(),
        wasCheckedin = boolean().next(),
    )
}

fun Arb.Companion.category() = arbitrary {
    string(minSize = 1, maxSize = 5, codepoints = Codepoint.az()).next()
}

fun Arb.Companion.freetrainingDbo() = arbitrary {
    FreetrainingDbo(
        id = int(min = 1).next(),
        name = string(minSize = 5, maxSize = 20).next(),
        category = category().next(),
        date = localDate().next(),
        venueId = int(min = 1).next(),
        wasCheckedin = boolean().next(),
    )
}

fun Arb.Companion.singlesDbo() = arbitrary {
    SinglesDbo(
        notes = string().next(),
        lastSync = localDateTime().orNull().next(),
    )
}
