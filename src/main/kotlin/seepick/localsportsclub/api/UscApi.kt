package seepick.localsportsclub.api

import kotlinx.serialization.Serializable

@Serializable
data class StudiosMapJsonRoot(
    val success: Boolean,
    val data: StudiosMapJsonObject,
)

@Serializable
data class StudiosMapJsonObject(
    val venues: List<StudiosMapVenue>
)

@Serializable
data class StudiosMapVenue(
    val id: Int,
    val name: String,
    val address: String,
    // addressId: Int,
    /** Used to calculate the details URL. */
    val slug: String,
    // location: lat/lng
    // district: West
    // planTypeIds
    // studioCovers
    // categories
    // - id, key, name, is_top_category, icon, category_group_id, translations, plan types...
    // featured
)

// ---
@Serializable
data class VenuesJsonRoot(
    val success: Boolean,
    val data: VenuesDataJson
)

@Serializable
data class VenuesDataJson(
    val showMore: Boolean, // FIXME request more when this is true!
    // TODO ... add more content ...
)

// ---
enum class District(val label: String, val id: Int, val parent: Int?) {
    Amsterdam("Amsterdam", 8749, null),
    Amsterdam_Centrum("Centrum", 8777, Amsterdam.id),
}

enum class PlanTypes(val id: Int, val label: String) {
    Small(1, "S"),
    Medium(2, "M"),
    Large(3, "L"),
    ExtraLarge(6, "XL"),
}

