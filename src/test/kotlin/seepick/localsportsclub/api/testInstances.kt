package seepick.localsportsclub.api

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.api.venue.VenueInfo

fun Arb.Companion.venueInfo() = arbitrary {
    VenueInfo(
        title = string(minSize = 3, maxSize = 30, codepoints = Codepoint.alphanumeric()).next(),
        slug = string(minSize = 3, maxSize = 6, codepoints = Codepoint.alphanumeric()).next(),
        imageUrl = "https://${string(minSize = 3, maxSize = 8, codepoints = Codepoint.az()).next()}.com",
        disciplines = list(string(minSize = 3, maxSize = 8, codepoints = Codepoint.az()), 0..3).next(),
        addressId = int(min = 1, max = 999).next(),
        addressDistrict = string(minSize = 3, maxSize = 5, codepoints = Codepoint.alphanumeric()).next(),
        addressStreet = string(minSize = 5, maxSize = 10, codepoints = Codepoint.alphanumeric()).next(),
    )
}
