package seepick.localsportsclub.api.venue

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import seepick.localsportsclub.kotlinxSerializer

data class VenueDetails(
    val title: String, // a.k.a. "name"
    val slug: String,
    val description: String,
    val disciplines: List<String>,
    val linkedVenueSlugs: List<String>,
    val websiteUrl: Url?,
    val importantInfo: String?,
    val openingTimes: String?,
    val postalCode: String, // from JSON script
    val streetAddress: String, // from JSON script
    val addressLocality: String, // from JSON script
    val latitude: String, // from JSON script
    val longitude: String,
    val originalImageUrl: Url?, // from JSON script
    // multiple pictures ... maybe in the future
    // visit limits ... always the same
)

@Serializable
data class VenueDetailEmbedJson(
    val telephone: String,
    val image: String,
    val address: VenueDetailEmbedAddress,
    val geo: VenueDetailEmbedGeo
)

@Serializable
data class VenueDetailEmbedAddress(
    val postalCode: String,
    val streetAddress: String,
    val addressLocality: String,
)

@Serializable
data class VenueDetailEmbedGeo(
    val latitude: String,
    val longitude: String,
)

object VenueDetailsParser {

    private object EnglishLabels {
        const val OTHER_LOCATIONS = "Other Locations:"
        const val WEBSITE = "Website:"
        const val IMPORTANT_INFO = "Important Info:"
        const val OPENING_TIMES = "Opening Times:"
    }

    private val importantInfoDefaults = listOf(".", "-")
    private const val OPENING_TIMES_DEFAULT_VALUE_NL =
        "De openingstijden zijn afhankelijk van de cursustijden/afspraken, of zijn niet bekend. Je kunt meer informatie vinden op de partnerwebsite."
    private const val OPENING_TIMES_DEFAULT_VALUE_EN =
        "The opening hours depend on the course times / agreed appointments or are not known. You can find more information on the partner website."
    private const val NO_IMAGE_SET_URL = "https://urbansportsclub.com/images/merchant/venueHome.jpg"

    fun parse(htmlString: String): VenueDetails {
        val document = Jsoup.parse(htmlString)
        val html = document.childNodes().single { it.nodeName() == "html" }
        val head = html.childNodes().single { it.nodeName() == "head" } as Element
        val body = html.childNodes().single { it.nodeName() == "body" } as Element

        val title = body.select("h1").text()
        val linkedVenues = mutableListOf<String>()
        var website: String? = null
        var openingTimes: String? = null
        var importantInfo: String? = null
        val json = body.select("script[type=\"application/ld+json\"]").first()!!.dataNodes().first().wholeData
        val detail = kotlinxSerializer.decodeFromString<VenueDetailEmbedJson>(json)
        val slug = head.select("meta[property=\"og:url\"]").attr("content").substringAfterLast("/")
        val disciplines = body.select("div.disciplines").text().split(",").map { it.trim() }
        val description = body.select("p.description").text()
        body.select("div.studio-info-section").forEach { div ->
            when (div.select("h3").single().text()) {
                EnglishLabels.OTHER_LOCATIONS -> {
                    div.select("a").forEach { a ->
                        linkedVenues += a.attr("href").substringAfterLast("/")
                    }
                }

                EnglishLabels.WEBSITE -> {
                    website = div.select("a").first()!!.attr("href")
                }

                EnglishLabels.IMPORTANT_INFO -> {
                    importantInfo = div.select("p span.pre-line").text().trim().let {
                        if (importantInfoDefaults.contains(it)) null else it
                    }
                }

                EnglishLabels.OPENING_TIMES -> {
                    openingTimes = div.select("p span.pre-line").text().trim().let {
                        if (it == OPENING_TIMES_DEFAULT_VALUE_EN || it == OPENING_TIMES_DEFAULT_VALUE_NL) null else it
                    }
                }
            }
        }
        return VenueDetails(
            title = title,
            slug = slug,
            linkedVenueSlugs = linkedVenues,
            websiteUrl = website?.let { Url(it) },
            disciplines = disciplines,
            description = description,
            importantInfo = importantInfo,
            openingTimes = openingTimes,
            originalImageUrl = detail.image.let { if (it == NO_IMAGE_SET_URL) null else Url(it) },
            latitude = detail.geo.latitude,
            longitude = detail.geo.longitude,
            streetAddress = detail.address.streetAddress,
            addressLocality = detail.address.addressLocality,
            postalCode = detail.address.postalCode,
        )
    }
}
