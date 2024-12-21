package seepick.localsportsclub.api.domain

import seepick.localsportsclub.api.City
import java.net.URI

data class Venue(
    val id: Int,
    val name: String,
    val slug: String,
    val facilities: String,
    val city: City,
    val rating: Rating,
    val notes: String,
    val officialWebsite: URI?,
    val uscWebsite: URI, // inferred by static URL + slug
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
) {
    companion object {
        fun dummy() = Venue(
            id = 42,
            name = "Dummy Venue",
            slug = "dummy-venue",
            facilities = "Gym",
            city = City.Amsterdam,
            rating = Rating.R4,
            notes = "no notes",
            officialWebsite = null,
            uscWebsite = URI("https://usc.com/en/dummy-venue"),
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            isDeleted = false,
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
