package seepick.localsportsclub.view.venue.detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.model.Venue

class VenueEditModel {

    var officialWebsite: MutableState<String?> = mutableStateOf(null)
    var notes = mutableStateOf("")
    var rating = mutableStateOf(Rating.R0)
    var isFavorited = mutableStateOf(false)
    var isWishlisted = mutableStateOf(false)
    var isHidden = mutableStateOf(false)
    var currentVenue: Venue? = null

    fun init(venue: Venue) {
        currentVenue = venue
        officialWebsite.value = venue.officialWebsite
        notes.value = venue.notes
        rating.value = venue.rating
        isFavorited.value = venue.isFavorited
        isWishlisted.value = venue.isWishlisted
        isHidden.value = venue.isHidden
    }

    fun updatePropertiesOf(venue: Venue) {
        venue.notes = notes.value
        venue.rating = rating.value
        venue.isFavorited = isFavorited.value
        venue.isWishlisted = isWishlisted.value
        venue.isHidden = isHidden.value
        venue.officialWebsite = officialWebsite.value?.let { it.ifEmpty { null } }
    }

    fun isClean(): Boolean =
        currentVenue?.let { venue ->
            notes.value == venue.notes &&
                    officialWebsite.value == venue.officialWebsite &&
                    rating.value == venue.rating &&
                    isFavorited.value == venue.isFavorited &&
                    isWishlisted.value == venue.isWishlisted &&
                    isHidden.value == venue.isHidden
        } ?: false
}
