package seepick.localsportsclub.api.venue

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestJson

fun main() {
    val json = readTestJson<VenuesJson>("response_venues.json")

    println("Stats.Venues (${json.data.stats.venue.size}):")
    json.data.stats.venue.forEach {
        println("- $it")
    }
    println("Stats.Categories (${json.data.stats.category.size}):")
    json.data.stats.category.forEach {
        println("- $it")
    }
    println("Stats.District:")
    println(json.data.stats.district)
    val htmls = VenueParser.parseHtmlContent(json.data.content)
    println("htmls (${htmls.size}):")
    htmls.forEach {
        println("- $it")
    }
}

class VenueParserTest : StringSpec() {
    init {
        "When parse simplified venues Then parse data from HTML" {
            val result =
                VenueParser.parseHtmlContent(readTestJson<VenuesJson>("response_venues.simplified.json").data.content)

            result.shouldBeSingleton().first() shouldBe VenueInfo(
                addressId = 25678,
                slug = "amsterdam-west",
                imageUrl = "https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2FvenueCatalog_311x175_bmrtjlmxbveot0zlvwuj_1727358338834864.png?generation=1727358339345039&alt=media",
                title = "24Seven - Amsterdam West",
                disciplines = listOf("Bokssport", "Fitness"),
                addressDistrict = "West",
                addressStreet = "Herentalsstraat 132",
            )
        }
    }
}
