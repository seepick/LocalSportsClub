package seepick.localsportsclub

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.http.Url
import seepick.localsportsclub.api.City
import seepick.localsportsclub.api.PlanType
import seepick.localsportsclub.api.UscConfig

fun Arb.Companion.uscConfig() = arbitrary {
    UscConfig(
        baseUrl = url().next(),
        city = enum<City>().next(),
        plan = enum<PlanType>().next(),
        storeResponses = boolean().next(),
    )
}

fun Arb.Companion.usageConfig() = arbitrary {
    val maxBookingsPerPeriod = int(min = 6, max = 20).next()
    val maxBookingsForDay = int(min = 2, max = 5).next()
    UsageConfig(
        periodConfiguredFirstDay = int(min = 1, max = 30).next(),
        maxBookingsForPeriod = maxBookingsPerPeriod,
        maxBookingsPerVenueForMonth = int(min = 1, max = maxBookingsPerPeriod).next(),
        maxBookingsForDay = maxBookingsForDay,
        maxBookingsPerVenueForDay = int(min = 1, max = maxBookingsForDay).next(),
    )
}

fun Arb.Companion.url() = arbitrary {
    Url(
        "https://${
            string(
                minSize = 5,
                maxSize = 15,
                codepoints = Codepoint.alphanumeric()
            ).next()
        }.${string(minSize = 3, maxSize = 3, codepoints = Codepoint.az()).next()}"
    )
}

fun Arb.Companion.imageUrl() = arbitrary {
    val fileName =
        string(minSize = 3, maxSize = 15, codepoints = Codepoint.az()).next() + ".png"
    Url("${url().next()}/${fileName}")
}

fun Arb.Companion.slug() = arbitrary {
    string(minSize = 3, maxSize = 8, codepoints = Codepoint.alphanumeric()).next()
}
