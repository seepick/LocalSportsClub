package seepick.localsportsclub.api.venueDetails

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readFromClasspath

class VenueDetailsParserTest : StringSpec() {
    init {
        fun readAndParse(fileName: String) =
            VenueDetailsParser.parse(readFromClasspath("/test_lsc/$fileName"))

        "Given no linked venue and no website" {
            readAndParse("response_venue_detail-linked0.html") shouldBe VenueDetails(
                linkedVenueSlugs = emptyList(),
                websiteUrl = null,
            )
        }
        "Given one linked venue and website" {
            readAndParse("response_venue_detail-linked1.html") shouldBe VenueDetails(
                linkedVenueSlugs = listOf("aerials-amsterdam-lola"),
                websiteUrl = "https://www.aerials.amsterdam",
            )
        }
        "Given multiple linked venues and no website" {
            readAndParse("response_venue_detail-linkedn.html") shouldBe VenueDetails(
                linkedVenueSlugs = listOf(
                    "amsterdam-noord-binnen",
                    "amsterdam-noord",
                    "amsterdam-venserpolder",
                    "amsterdam-zuidoost"
                ),
                websiteUrl = null,
            )
        }
    }
}
