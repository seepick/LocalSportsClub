package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.slug
import testfixtUsc.dateTimeRange

fun Arb.Companion.location() = arbitrary {
    Location(double(min = 4.0, max = 6.0).bind(), double(min = 3.0, max = 4.0).bind())
}

fun Arb.Companion.venue() = arbitrary {
    Venue(
        id = int(min = 1).bind(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).bind(),
        slug = slug().bind(),
        description = string(minSize = 5, maxSize = 20).bind(),
        notes = string(minSize = 5, maxSize = 20).bind(),
        categories = emptyList(),
        city = City.all.random(),
        postalCode = "",
        addressLocality = "",
        street = "",
        location = location().bind(),
        distanceInKm = double(min = 0.1, max = 13.0).bind(),
        imageFileName = string(minSize = 3, maxSize = 20, codepoints = Codepoint.az()).orNull().bind(),
        importantInfo = null,
        openingTimes = null,
        uscWebsite = "",
        officialWebsite = null,
        isDeleted = boolean().bind(),
        isFavorited = boolean().bind(),
        isWishlisted = boolean().bind(),
        isHidden = boolean().bind(),
        rating = Rating.entries.random(),
        plan = enum<Plan.UscPlan>().bind(),
        isAutoSync = boolean().bind(),
    )
}

fun Arb.Companion.activity() = arbitrary {
    val dateTimeRange = dateTimeRange().bind()
    Activity(
        id = int(min = 1).bind(),
        venue = venue().bind(),
        name = string(minSize = 5, maxSize = 20, codepoints = Codepoint.az()).bind(),
        category = string(minSize = 3, maxSize = 5, codepoints = Codepoint.az()).bind(),
        dateTimeRange = dateTimeRange,
        spotsLeft = int(min = 0, max = 20).bind(),
        teacher = string(minSize = 2, maxSize = 25, codepoints = Codepoint.az()).orNull().bind(),
        state = enum<ActivityState>().bind(),
        cancellationLimit = if (boolean().bind()) null else dateTimeRange.from.minusHours(2),
        description = string().orNull().bind(),
        plan = enum<Plan.UscPlan>().bind(),
    )
}
