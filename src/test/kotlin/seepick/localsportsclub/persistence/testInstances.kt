package seepick.localsportsclub.persistence

import com.github.seepick.uscclient.credentials
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
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
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences

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
        longitude = double(0.0, 5.0, false).bind().toString().take(5),
        latitude = double(0.0, 5.0, false).bind().toString().take(5),
        planId = enum<Plan.UscPlan>().bind().id,
        isAutoSync = boolean().bind()
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
        state = enum<ActivityState>().bind(),
        cancellationLimit = if (boolean().bind()) null else from.minusHours(2),
        planId = enum<Plan.UscPlan>().bind().id,
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
        state = enum<FreetrainingState>().bind(),
        planId = enum<Plan.UscPlan>().bind().id,
    )
}

fun Arb.Companion.singlesDbo() = arbitrary {
    SinglesDbo(
        version = int().bind(),
        json = string().bind(),
    )
}

@Suppress("unused")
fun Arb.Companion.windowPref() = arbitrary {
    WindowPref(
        width = int(min = 0).bind(),
        height = int(min = 0).bind(),
        posX = int(min = 0).bind(),
        posY = int(min = 0).bind(),
    )
}

fun Arb.Companion.preferences() = arbitrary {
    Preferences(
        uscCredentials = credentials().orNull().bind(),
        city = City.all.random(),
        home = location().orNull().bind(),
        gcal = gcal().bind(),
        periodFirstDay = int(min = 1, max = 28).orNull().bind(),
    )
}

fun Arb.Companion.gcal() = arbitrary {
    if (boolean().bind()) {
        Gcal.GcalDisabled
    } else {
        Gcal.GcalEnabled(calendarId = string(maxSize = 64).bind())
    }
}

fun Arb.Companion.location() = arbitrary {
    Location(
        latitude = double(min = -90.0, max = 90.0).bind(),
        longitude = double(min = -180.0, max = 180.0).bind(),
    )
}

