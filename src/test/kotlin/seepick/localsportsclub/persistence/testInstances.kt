package seepick.localsportsclub.persistence

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
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Credentials
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences

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
        state = enum<ActivityState>().next(),
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
        state = enum<FreetrainingState>().next(),
    )
}

fun Arb.Companion.singlesDbo() = arbitrary {
    SinglesDbo(
        version = int().next(),
        json = string().next(),
    )
}

fun Arb.Companion.windowPref() = arbitrary {
    WindowPref(
        width = int(min = 0).next(),
        height = int(min = 0).next(),
        posX = int(min = 0).next(),
        posY = int(min = 0).next(),
    )
}

fun Arb.Companion.preferences() = arbitrary {
    Preferences(
        uscCredentials = credentials().orNull().next(),
        city = City.all.random(),
        home = location().orNull().next(),
        gcal = gcal().next(),
        periodFirstDay = int(min = 1, max = 28).orNull().next(),
    )
}

fun Arb.Companion.gcal() = arbitrary {
    if (boolean().next()) {
        Gcal.GcalDisabled
    } else {
        Gcal.GcalEnabled(calendarId = string(maxSize = 64).next())
    }
}

fun Arb.Companion.location() = arbitrary {
    Location(
        latitude = double(min = -90.0, max = 90.0).next(),
        longitude = double(min = -180.0, max = 180.0).next(),
    )
}

fun Arb.Companion.credentials() = arbitrary {
    Credentials(
        username = string(minSize = 3, maxSize = 64).next(),
        password = string(minSize = 3, maxSize = 50).next(), // 128 max, but will be encrypted
    )
}
