package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface SimpleVenue {
    val id: Int
    val slug: String
    val name: String
    val imageFileName: String?

    var rating: Rating
    var isWishlisted: Boolean
    var isFavorited: Boolean
    var isHidden: Boolean

    fun updateSelfBy(venue: Venue) {
        rating = venue.rating
        isWishlisted = venue.isWishlisted
        isFavorited = venue.isFavorited
        isHidden = venue.isHidden
    }
}

class SimpleVenueImpl(
    override val id: Int,
    override val slug: String,
    override val name: String,
    override val imageFileName: String?,
    rating: Rating,
    isWishlisted: Boolean,
    isFavorited: Boolean,
    isHidden: Boolean,
) : SimpleVenue {
    override var rating: Rating by mutableStateOf(rating)
    override var isWishlisted: Boolean by mutableStateOf(isWishlisted)
    override var isFavorited: Boolean by mutableStateOf(isFavorited)
    override var isHidden: Boolean by mutableStateOf(isHidden)
    
    override fun toString() = "SimpleVenueImpl[id=$id, name=$name, rating=$rating]"
}
