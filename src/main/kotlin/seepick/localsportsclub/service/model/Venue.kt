package seepick.localsportsclub.service.model

import io.ktor.http.Url
import seepick.localsportsclub.api.City

data class SimpleVenue(
    val id: Int,
    val slug: String,
    val name: String,
)

data class Venue(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val facilities: List<String>,
    val city: City,
    val rating: Rating,
    val notes: String,
    val postalCode: String,
    val street: String,
    val addressLocality: String,
    val latitude: String,
    val longitude: String,
    val officialWebsite: Url?,
    val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: Url, // inferred by static URL + slug
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
//    val linkedVenues: List<SimpleVenue>,
) {
    companion object {
        fun dummy() = Venue(
            id = 42,
            name = "Dummy Venue",
            slug = "dummy-venue",
            facilities = listOf("Gym"),
            city = City.Amsterdam,
            rating = Rating.R4,
            notes = "no notes",
            officialWebsite = null,
            description = "dummy description",
            openingTimes = null,
            importantInfo = null,
            imageFileName = null,
            uscWebsite = Url("https://usc.com/en/dummy-venue"),
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            isDeleted = false,
            postalCode = "1001",
            street = "Street",
            addressLocality = "Amsterdam, Netherlands",
            latitude = "0.1",
            longitude = "0.2",
//            linkedVenues = emptyList(),
        )
    }
}

class Rating private constructor(val value: Int) : Comparable<Rating> {
    companion object {
        private val ratingByValue by lazy {
            entries.associateBy { it.value }
        }

        fun byValue(rating: Int): Rating =
            ratingByValue[rating] ?: error("Invalid rating value: $rating")

        val R0 = Rating(0)
        val R1 = Rating(1)
        val R2 = Rating(2)
        val R3 = Rating(3)
        val R4 = Rating(4)
        val R5 = Rating(5)
        val entries = listOf(R0, R1, R2, R3, R4, R5)
    }

    val string = (0..<value).fold("") { acc, _ -> "${acc}⭐️" }
    override operator fun compareTo(other: Rating): Int =
        value.compareTo(other.value)

    override fun toString(): String = "Rating$value"
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Rating) return false
        return value == other.value
    }
}
