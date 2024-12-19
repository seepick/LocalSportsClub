package seepick.localsportsclub.api.venueDetails

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class VenueDetails(
    // main description
    // multiple pictures
    val linkedVenueSlugs: List<String>,
    val websiteUrl: String?,
    // important info
    // visit limits
)

object VenueDetailsParser {

    private object EnglishLabels {
        val otherLocations = "Other Locations:"
        val website = "Website:"
    }

    fun parse(htmlString: String): VenueDetails {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes().single { it.nodeName() == "html" }
        val body = html.childNodes().single { it.nodeName() == "body" } as Element

        val linkedVenues = mutableListOf<String>()
        var website: String? = null
        body.select("div.studio-info-section").forEach { div ->
            when (div.select("h3").single().text()) {
                EnglishLabels.otherLocations -> {
                    div.select("a").forEach { a ->
                        linkedVenues += a.attr("href").substringAfterLast("/")
                    }
                }

                EnglishLabels.website -> {
                    website = div.select("a").first()!!.attr("href")
                }
            }
        }
        return VenueDetails(
            linkedVenueSlugs = linkedVenues,
            websiteUrl = website
        )
    }
}
