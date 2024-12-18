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
    val note: String,
    val officialWebsite: URI?,
    val uscWebsite: URI, // inferred by static URL + slug
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
)

enum class Rating(val value: Int) {
    R0(0), R1(1), R2(2), R3(3), R4(4), R5(5)
}
