package seepick.localsportsclub.api.venue

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.http.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object VenueParser {
    private val log = logger {}
    private const val noImageSet = "/images/merchant/venueCatalog.jpg"

    fun parseHtmlContent(htmlString: String): List<VenueInfo> {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes()[0] as Element
        val body = html.children()[1]
        val children = body.children()
        log.debug { "Parsing ${children.size} venues." }
        return children.map { div ->
            VenueInfo(
                title = div.select("p.smm-studio-snippet__title").text().trim(),
                slug = div.select("a.smm-studio-snippet__studio-link").attr("href").substringAfterLast("/"),
                imageUrl = div.select("img.smm-studio-snippet__lazy-image").attr("data-src").let {
                    if (it == noImageSet) null else Url(it)
                },
                disciplines = div.select("div.disciplines").text().trim().split("·").map { it.trim() },
                addressId = div.attr("data-address-id").toInt(),
                addressDistrict = div.select("p.smm-studio-snippet__address").text().trim().substringBefore(","),
                addressStreet = div.select("span.smm-studio-snippet__address-street").text().trim(),
            )
        }
    }
}

data class VenueInfo(
    val title: String,
    val slug: String, // e.g.: "/nl/venues/{slug}"
    val imageUrl: Url?,
    val disciplines: List<String>,
    val addressId: Int,
    val addressDistrict: String,
    val addressStreet: String,
)
