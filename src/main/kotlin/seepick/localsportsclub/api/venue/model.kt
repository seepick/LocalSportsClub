package seepick.localsportsclub.api.venue

import kotlinx.serialization.Serializable
import seepick.localsportsclub.api.Pageable

@Serializable
data class VenuesJson(
    val success: Boolean,
    val data: VenuesJsonPage
)

@Serializable
data class VenuesJsonPage(
    override val showMore: Boolean,
    val content: String, // looooots of HTML
    val stats: VenuesStatsJson,
    val searchExecutedEvent: String, // big JSON
    val regionSelectorSelected: String? // TODO what type?!
) : Pageable

@Serializable
data class VenuesStatsJson(
    val category: List<VenueCategoryJson>,
    val district: VenueDistrictJson,
    val venue: List<VenueStatsVenueJson>,
)

@Serializable
data class VenueStatsVenueJson(
    val name: String,
    val attributes: VenueStatsVenueAttributes,
)

@Serializable
data class VenueStatsVenueAttributes(
    val value: Int,
)

@Serializable
data class VenueDistrictJson(
    val district: List<String>, // TODO what type is this?
    val areas: List<VenueAreaJson>,
)

@Serializable
data class VenueAreaJson(
    val name: String,
    val attributes: VenueAreaAttributeJson,
    val districts: List<VenueAreaDistrictJson>,
)

@Serializable
data class VenueAreaAttributeJson(
    val value: Int,
    val `class`: String,
)

@Serializable
data class VenueAreaDistrictJson(
    val name: String,
    val attributes: VenueAreaAttributeJson,
)

@Serializable
data class VenueCategoryJson(
    val name: String,
    val attributes: VenueAttributesJson,
)

@Serializable
data class VenueAttributesJson(
    val value: String,
)

