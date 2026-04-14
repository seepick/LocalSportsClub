package lsc.repo

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.localDate
import io.kotest.property.arbitrary.localDateTime
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

fun Arb.Companion.venueDbo() = arbitrary {
    val id = int(min = 1).bind()
    VenueDbo(
        id = id,
        name = string(minSize = 3, maxSize = 20, codepoints = Codepoint.alphanumeric()).bind(),
        slug = string(minSize = 3, maxSize = 8, codepoints = Codepoint.alphanumeric()).bind(),
        facilities = string(minSize = 0, maxSize = 20, codepoints = Codepoint.alphanumeric()).bind(),
        cityId = int(min = 1).bind(),
        officialWebsite = string(minSize = 5, maxSize = 20, codepoints = Codepoint.alphanumeric()).orNull().bind(),
        rating = 0,
        description = string(minSize = 5, maxSize = 50).bind(),
        openingTimes = string(minSize = 5, maxSize = 50).orNull().bind(),
        importantInfo = string(minSize = 5, maxSize = 50).orNull().bind(),
        imageFileName = if (boolean().bind()) "$id.png" else null,
        notes = string(minSize = 0, maxSize = 20, codepoints = Codepoint.alphanumeric()).bind(),
        isFavorited = boolean().bind(),
        isWishlisted = boolean().bind(),
        isHidden = boolean().bind(),
        isDeleted = boolean().bind(),
        addressLocality = string(minSize = 5, maxSize = 50).bind(),
        street = string(minSize = 5, maxSize = 50).bind(),
        postalCode = string(minSize = 5, maxSize = 6).bind(),
        longitude = double(0.0, 50.0, false).bind(),
        latitude = double(0.0, 50.0, false).bind(),
        planId = int().bind(),
        isAutoSync = boolean().bind(),
        visitLimits = visitLimitsDbo().bind(),
        lastSync = localDate().orNull().bind(),
    )
}

fun Arb.Companion.activityDbo() = arbitrary {
    val from = localDateTime().bind()
    ActivityDbo(
        id = int(min = 1).bind(),
        venueId = int(min = 1).bind(),
        name = string(minSize = 5, maxSize = 20).bind(),
        category = category().bind(),
        spotsLeft = int(min = 0, max = 10).bind(),
        from = from,
        to = from.plusMinutes(long(min = 30, max = 120).bind()),
        teacher = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().bind(),
        description = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().bind(),
        state = enum<ActivityStateDbo>().bind(),
        cancellationLimit = if (boolean().bind()) null else from.minusHours(2),
        planId = int().bind(),
    )
}

fun Arb.Companion.category() = arbitrary {
    string(minSize = 1, maxSize = 5, codepoints = Codepoint.az()).bind()
}

fun Arb.Companion.freetrainingDbo() = arbitrary {
    FreetrainingDbo(
        id = int(min = 1).bind(),
        name = string(minSize = 5, maxSize = 20).bind(),
        category = category().bind(),
        date = localDate().bind(),
        venueId = int(min = 1).bind(),
        state = enum<FreetrainingStateDbo>().bind(),
        planId = int().bind(),
    )
}

fun Arb.Companion.singlesDbo() = arbitrary {
    SinglesDbo(
        version = int().bind(),
        json = string().bind(),
    )
}
